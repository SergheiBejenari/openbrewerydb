## Additional scenarios for `/search`

1. **Special characters in queries**
    - **Why**: Real names include `- ' . & /`
    - **Expectation**: No server errors; either valid matches or a clean empty list.

2. **Ordering stability (identical requests)**
    - **Why**: Unstable order breaks pagination UX
    - **Expectation**: Two identical requests return the same first page.

3. **Cross-page windowing (observed-behavior smoke)**
    - **Why**: Users shouldn’t see repeats when paging
    - **Expectation**: For a high-yield query, page 1 vs page 2 have no overlapping IDs.
    - **Note**: Because `/search` pagination isn’t documented, this is a smoke check rather than a strict contract test.

4. **Result quality guardrail**
    - **Why**: Keep the top of the list meaningful
    - **Expectation**: In the top-10 results, the majority contain all normalized query tokens; irrelevant items don’t dominate.

---

#  List Breweries — Automation Approach & Test Design

**Endpoint**: `GET /v1/breweries`

### Approach

- **Table-driven tests (DataProviders)**: one parameterized test per behavior family — pagination, sorting, distance, single-filter, pairwise filters.

### Test-design techniques

- **Equivalence Partitioning (EP)**: valid vs invalid values for each filter (e.g., `by_state` full name vs abbreviation; `by_postal` shape; `by_type` allowed vs unknown)
- **Boundary Value Analysis (BVA)**: pagination edges (`per_page` and `page`)
- **Pairwise combinations**: a few high-ROI two-filter intersections; add only a couple of targeted three-way cases where risk is high (e.g., filter × filter × sort)
- **Compact input tables**: fed to DataProviders

**Examples:**
- `per_page`: 1, 50 (default), 200 (max), 201 (over-limit)
- `page`: 1, 2, 99999 (beyond dataset), 0, -1, "abc"
- `sort`: fields `{name, city, state, country, type}` with `{asc, desc}`, plus multi-field `name,city`
- `by_state`: valid `{California, New_York}`, invalid `{CA}`
- `by_postal`: valid `{92101, 94107-1234, 94107_1234}`, invalid `{941071234}`
- `by_type` (enum): full documented set, incl. deprecated values
- `by_city` / `by_country` / `by_name`: include `San_Diego` and `San%20Diego`
- `by_ids`: `[valid, valid]`, `[valid, invalid]`, `[invalidOnly]`
- `by_dist`: valid `{lat,lon}`, invalid formats/out-of-range

---

##  Scenarios

### LB-001 · Single-filter conformance (table-driven)
- **Goal**: One parameterized test verifies all filters
- **Positive expectations**: correct values returned for each filter
- **Negative expectations**: unknown/invalid values → empty list or 4xx

### LB-002 · Pagination — `page` (BVA)
- **Goal**: Stable navigation across pages
- **Expected behavior**:
    - Page=1 vs Page=2: non-overlapping sets
    - Page=99999: empty list, not error
    - Page=0, negatives, or non-numeric: 4xx or normalization (no server errors)

### LB-003 · Sorting (single & multi-field)
- **Goal**: Predictable ordering
- **Expected behavior**:
    - Sorted monotonic in requested direction
    - Multi-field `name,city`: tie-breaker works
    - Paging preserves global order
    - Sort not combined with `by_dist`

### LB-004 · Distance ordering — `by_dist=lat,lon`
- **Goal**: Location-based ordering
- **Expected behavior**:
    - Sorted by ascending distance
    - Invalid/out-of-range → 4xx
    - If sort is provided, incompatibility respected

### LB-005 · Pagination — `per_page` (BVA)
- **Goal**: Enforce default and limits
- **Expected behavior**:
    - Default: ≤ 50 items
    - Valid values (1, 50, 200): ≤ requested size
    - Over-limit (201): 4xx or clamp to 200 (no server errors)

### LB-006 · Pairwise filters (high ROI)
- **Goal**: Validate common two-filter intersections
- **Pairs**: `by_state × by_type`, `by_country × by_city`, `by_postal × by_type`, `by_name × by_state`
- **Expected behavior**: all returned items satisfy both filters, order preserved

**Targeted three-way additions (optional):**
- `by_state × by_type × sort=name:asc`
- `by_postal × by_type × per_page=200`

---

##  Effort Estimate

- **Scaffolding & utilities** — 2–3 hours
- **Pagination & sorting core** — 2–4 hours
- **Distance & filters** — 2–4 hours
- **Pairwise tests & polish** — 1–2 hours

**Total (net): ~7–13 hours**

---
