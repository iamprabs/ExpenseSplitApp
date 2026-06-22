---
name: Lumina Finance
colors:
  surface: '#f7f9fb'
  surface-dim: '#d8dadc'
  surface-bright: '#f7f9fb'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f4f6'
  surface-container: '#eceef0'
  surface-container-high: '#e6e8ea'
  surface-container-highest: '#e0e3e5'
  on-surface: '#191c1e'
  on-surface-variant: '#46464f'
  inverse-surface: '#2d3133'
  inverse-on-surface: '#eff1f3'
  outline: '#767680'
  outline-variant: '#c7c5d1'
  surface-tint: '#535a92'
  primary: '#131b50'
  on-primary: '#ffffff'
  primary-container: '#2a3166'
  on-primary-container: '#949ad7'
  inverse-primary: '#bcc2ff'
  secondary: '#006d36'
  on-secondary: '#ffffff'
  secondary-container: '#6dfe9c'
  on-secondary-container: '#007439'
  tertiary: '#490013'
  on-tertiary: '#ffffff'
  tertiary-container: '#700223'
  on-tertiary-container: '#fc7185'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dfe0ff'
  primary-fixed-dim: '#bcc2ff'
  on-primary-fixed: '#0e154b'
  on-primary-fixed-variant: '#3b4278'
  secondary-fixed: '#6dfe9c'
  secondary-fixed-dim: '#4de082'
  on-secondary-fixed: '#00210c'
  on-secondary-fixed-variant: '#005227'
  tertiary-fixed: '#ffdadc'
  tertiary-fixed-dim: '#ffb2b9'
  on-tertiary-fixed: '#400010'
  on-tertiary-fixed-variant: '#891933'
  background: '#f7f9fb'
  on-background: '#191c1e'
  surface-variant: '#e0e3e5'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 57px
    fontWeight: '700'
    lineHeight: 64px
    letterSpacing: -0.25px
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
  title-lg:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '500'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.1px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.5px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 4px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  gutter: 16px
  margin-mobile: 16px
  margin-desktop: 24px
---

## Brand & Style

The design system is anchored in the **Corporate / Modern** aesthetic, specifically leveraging the adaptive principles of Material You to ensure a native Android feel. The personality is defined by reliability, precision, and clarity, targeting users who require a high-trust environment for managing wealth and debt. 

The emotional response is one of "organized calm." By utilizing a disciplined grid and purposeful color application, the UI minimizes cognitive load. The style avoids unnecessary decoration in favor of functional clarity, using subtle tonal shifts to indicate hierarchy and security. The interface feels established yet technologically forward, bridging the gap between traditional banking and modern fintech.

## Colors

The palette is strategically weighted to reinforce financial health signals. 

- **Primary (Deep Indigo):** Used for structural elements, primary actions, and branding to establish authority and trust.
- **Secondary (Mint Green):** Strictly reserved for positive growth, credits, and success states. It serves as a visual reward.
- **Tertiary (Coral):** Utilized for debts, expenses, and alerts. Its warmth prevents it from feeling overly aggressive while maintaining high visibility.
- **Neutrals:** A range of soft grys and whites create a clean canvas, ensuring that the semantic colors (green/coral) are immediately meaningful.

Color usage must follow a 60-30-10 distribution, where the neutral background dominates to ensure high readability and a "breathable" interface.

## Typography

This design system utilizes **Inter** for all roles to maximize legibility and maintain a systematic, utilitarian feel. 

- **Numerical Data:** Financial figures should use `medium` or `semibold` weights to ensure they are the primary focal point of any view.
- **Hierarchy:** Use `title-lg` for card headers and `body-md` for secondary supporting text.
- **Mobile Scaling:** Large displays for balances use `display-lg` on desktop, but must scale down to `headline-lg-mobile` to prevent horizontal scrolling on smaller Android devices.
- **Letter Spacing:** Labels and small captions use slightly increased letter spacing (0.5px) to maintain clarity at small sizes.

## Layout & Spacing

The layout follows an **8px grid system** for consistent alignment and rhythm. 

- **Fluid Grid:** On Android, content should stretch to fill the screen width using a 4-column grid for mobile and an 8 or 12-column grid for tablets.
- **Safe Areas:** Ensure a minimum 16px margin on the left and right of the screen to prevent content from hitting the bezel.
- **Touch Targets:** All interactive elements must maintain a minimum height of 48dp to ensure accessibility and ease of use for quick financial transactions.
- **Vertical Rhythm:** Group related financial data (e.g., a list of transactions) using 8px spacing, while separating distinct sections (e.g., Accounts vs. Recent Activity) with 24px or 32px.

## Elevation & Depth

In alignment with Material You, elevation is communicated through **Tonal Layers** rather than heavy shadows. 

1. **Surface 0 (Background):** Used for the main app canvas (#F8FAFC).
2. **Surface 1 (Primary Cards):** A pure white background with a very subtle 1px border (#E2E8F0) to define boundaries.
3. **Surface 2 (Active States):** Uses a slight primary-tinted overlay (5% Deep Indigo) to indicate an element is being interacted with.
4. **Shadows:** Only used for floating action buttons (FAB) or high-priority modals. Shadows should be soft, diffused, and slightly tinted with the primary indigo color to prevent a "dirty" gray appearance.

## Shapes

The shape language is **Rounded**, reflecting the modern Android aesthetic. 

- **Small Components:** Checkboxes and small input tags use a 4px (Soft) radius.
- **Standard Components:** Buttons and list items use an 8px (Rounded) radius.
- **Large Components:** Dashboard cards and modals use a 16px (Rounded-LG) or 24px (Rounded-XL) radius to feel approachable and modern.
- **FAB:** The Floating Action Button should be a signature "squircle" or fully rounded pill to signify its primary status.

## Components

- **Buttons:** Primary buttons are solid Deep Indigo with white text. Secondary buttons for "Add Funds" or "Save" use a Mint Green outline.
- **Cards:** Transaction cards should be flat with a 1px border. The right-aligned amount should use Mint Green for positive values and Coral for negative values.
- **Input Fields:** Use the "Outlined" Material style. The label should float to the top border on focus. On error, the border and helper text transition to Coral.
- **Chips:** Used for filtering transaction categories (e.g., "Food", "Rent"). Use a light gray background that transitions to a light indigo tint when selected.
- **Lists:** Transaction lists should be dense, using 56dp row heights with clear iconography on the left to denote the category.
- **Progress Bars:** Savings goals should use Mint Green for the filled portion to reinforce the positive progress.
- **Bottom Navigation:** Uses the standardized Android navigation bar with the Primary Deep Indigo for the active icon state.