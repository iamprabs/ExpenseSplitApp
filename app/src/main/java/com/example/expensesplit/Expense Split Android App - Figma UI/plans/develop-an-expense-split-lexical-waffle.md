# Expense Split App — Implementation Plan

## Context
Build a comprehensive Expense Split app rendered as a mobile-first React web app simulating an Android UI. The app covers group management, expense logging, debt tracking, balance visualization, analytics, multi-currency, receipt scanning, and recurring expenses. This is a single-file React component in `src/app/App.tsx`.

## Aesthetic Stance
**Fintech Premium** — data-dense but refined. Not SaaS-modern gray. Commitment:
- **Ground:** True white (`#FFFFFF`) with `#F4F6FB` section backgrounds
- **Primary:** Deep navy `#1A237E` (trust, authority)
- **Accent:** Vibrant teal `#00BFA5` (positive balances/settlements)
- **Danger/Debt:** Coral `#FF5252`
- **Typography:** `Plus Jakarta Sans` (display + UI) + `JetBrains Mono` (amounts, numbers, currency codes)
- **Radius:** `0.875rem` — rounded but not bubbly
- **Shadow:** Layered soft shadows for card depth

## Architecture — Single-File React App
All state managed via `useState` / `useReducer` in `App.tsx`. No routing library — a `currentView` state string drives the "screen" shown. Simulates Android bottom-nav app with 5 tabs.

## Screens / Views
1. **Dashboard** (Home) — net balance summary, recent activity, quick-add button
2. **Groups** — list of groups, create group modal, group detail
3. **Friends** — friend list, add friend, per-friend balance
4. **Activity** — full expense feed with search, filters (category, date, amount)
5. **Analytics** — recharts pie + bar charts, spending by category/month/person

## Key Feature Implementation

### Data Model (in-memory state)
```ts
type Friend = { id, name, avatar, email, currency }
type Group = { id, name, icon, members: Friend[], color }
type Expense = {
  id, title, amount, currency, category, paidBy, splitWith,
  splitType: 'equal'|'custom'|'percent'|'shares',
  splits: Record<friendId, number>,
  date, groupId?, isRecurring, recurringInterval?,
  receiptUrl?, notes, tags
}
type Balance = Record<friendId, number>  // positive = owed to you, negative = you owe
```

### Bottom Navigation
5 tabs: Home, Groups, Friends, Activity, Analytics
Fixed at bottom, active tab highlighted with primary color icon + label.

### Modals (using @radix-ui/react-dialog)
- Add Expense — amount, title, paid-by, split-with, split-type, category, currency
- Add Group — name, icon, color picker, add members
- Add Friend — name, email, avatar color
- Debt Simplification view — computed min-transactions graph
- Receipt Scanner — simulated OCR with fake result after 2s
- Import CSV — drag-drop zone (simulated)

### Split Logic
- **Equal:** amount / n per person
- **Custom:** user enters exact amounts (validated to sum to total)
- **Percent:** user enters percentages (validated to sum to 100%)
- **Shares:** user enters share counts, proportional distribution

### Debt Simplification Algorithm
Classic min-cash-flow: compute net balance per person, greedily match largest creditor to largest debtor, reduce to minimum transactions. Implemented as a pure function `simplifyDebts(balances)`.

### Balance Calculation
For each expense: paidBy person gets credit for full amount minus their own split share; each other person owes their split share. Aggregate across all expenses for net balances.

### Recurring Expenses
Flag on expense + `recurringInterval: 'weekly'|'monthly'|'yearly'`. UI shows recurring badge. Simulated auto-generation shown in activity feed.

### Multi-Currency
100+ currency list with codes and symbols. Exchange rates table (static realistic rates against USD). Amounts converted to "base currency" for balance summation.

### Spending Analytics (recharts)
- Pie chart: spending by category
- Bar chart: monthly spending trend (6 months)
- Bar chart: spending by person
- All charts themed to match palette

### Search & Filter
Activity screen: search input filtering by title, person name, amount, category. Filter chips for category and date range.

### Visual Analytics Cards on Dashboard
- Net balance chip (green/red)
- "You owe" and "Owed to you" summary
- Recent 3 expenses
- Debt simplification quick view

### Simulated Features
- **Receipt scanning:** click → 2s loading → populates amount+title
- **CSV import:** drag zone → success toast
- **Offline:** banner toggle (simulated)
- **Payment integration:** UPI/PayPal/Venmo buttons on settlement modal
- **Premium badge:** shown in profile/settings tab

## Files to Modify
- `src/styles/fonts.css` — add Plus Jakarta Sans + JetBrains Mono from Google Fonts
- `src/styles/theme.css` — update tokens (background, foreground, primary, accent, radius, etc.)
- `src/app/App.tsx` — full implementation (~1000-1500 lines, comprehensive)

## Seed Data
Realistic placeholder data:
- 3 friends: Priya Sharma, Marcus Webb, Yuki Tanaka
- 3 groups: "Bali Trip 2025", "Flat 4B", "Office Lunch"
- 8-10 expenses across categories (Food, Travel, Home, Entertainment, Utilities)
- Pre-computed balances, realistic amounts, multiple currencies

## Token Updates
```css
--background: #FFFFFF;
--foreground: #0D1117;
--card: #FFFFFF;
--primary: #1A237E;
--primary-foreground: #FFFFFF;
--secondary: #F4F6FB;
--secondary-foreground: #0D1117;
--muted: #EEF0F6;
--muted-foreground: #6B7280;
--accent: #00BFA5;
--accent-foreground: #FFFFFF;
--destructive: #FF5252;
--border: rgba(0,0,0,0.08);
--radius: 0.875rem;
```

## Verification
1. All 5 tabs navigate correctly
2. Add Expense modal opens, fills, and saves — new expense appears in activity feed
3. Balance changes correctly after adding expense
4. Analytics charts render with real data
5. Search filters expense list correctly
6. Debt simplification shows reduced transaction set
7. Recurring expense badge visible
8. Currency selector shows 100+ options
9. Receipt scan simulation completes and populates form
10. Dark mode tokens intact in `.dark` block
