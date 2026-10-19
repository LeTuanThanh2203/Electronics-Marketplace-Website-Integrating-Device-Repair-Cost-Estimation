---
name: Technical Precision Ledger
colors:
  surface: '#f9f9f7'
  surface-dim: '#dadad8'
  surface-bright: '#f9f9f7'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f4f4f2'
  surface-container: '#eeeeec'
  surface-container-high: '#e8e8e6'
  surface-container-highest: '#e2e3e1'
  on-surface: '#1a1c1b'
  on-surface-variant: '#444748'
  inverse-surface: '#2f3130'
  inverse-on-surface: '#f1f1ef'
  outline: '#747878'
  outline-variant: '#c4c7c7'
  surface-tint: '#5f5e5e'
  primary: '#000000'
  on-primary: '#ffffff'
  primary-container: '#1c1b1b'
  on-primary-container: '#858383'
  inverse-primary: '#c9c6c5'
  secondary: '#b02600'
  on-secondary: '#ffffff'
  secondary-container: '#dc3200'
  on-secondary-container: '#fffbff'
  tertiary: '#000000'
  on-tertiary: '#ffffff'
  tertiary-container: '#1b1c19'
  on-tertiary-container: '#848480'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e5e2e1'
  primary-fixed-dim: '#c9c6c5'
  on-primary-fixed: '#1c1b1b'
  on-primary-fixed-variant: '#474646'
  secondary-fixed: '#ffdad2'
  secondary-fixed-dim: '#ffb4a2'
  on-secondary-fixed: '#3c0700'
  on-secondary-fixed-variant: '#8a1c00'
  tertiary-fixed: '#e4e2de'
  tertiary-fixed-dim: '#c7c6c2'
  on-tertiary-fixed: '#1b1c19'
  on-tertiary-fixed-variant: '#464744'
  background: '#f9f9f7'
  on-background: '#1a1c1b'
  surface-variant: '#e2e3e1'
typography:
  h1:
    fontFamily: Inter
    fontSize: 48px
    fontWeight: '700'
    lineHeight: '1.15'
    letterSpacing: -0.02em
  h1-mobile:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: '1.2'
    letterSpacing: -0.01em
  h2:
    fontFamily: Inter
    fontSize: 36px
    fontWeight: '700'
    lineHeight: '1.2'
    letterSpacing: -0.015em
  h2-mobile:
    fontFamily: Inter
    fontSize: 26px
    fontWeight: '700'
    lineHeight: '1.25'
    letterSpacing: -0.01em
  h3:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: '1.3'
    letterSpacing: -0.01em
  h4:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: '1.4'
  body:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.65'
  secondary:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: '1.5'
  navigation:
    fontFamily: Inter
    fontSize: 15px
    fontWeight: '500'
    lineHeight: '1.4'
  form-label:
    fontFamily: Inter
    fontSize: 15px
    fontWeight: '600'
    lineHeight: '1.3'
  input-text:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.5'
  button:
    fontFamily: Inter
    fontSize: 15px
    fontWeight: '600'
    lineHeight: '1.33'
  product-title:
    fontFamily: Inter
    fontSize: 17px
    fontWeight: '600'
    lineHeight: '1.35'
  product-price:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '700'
    lineHeight: '1.2'
  metadata-sm:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '500'
    lineHeight: '1.4'
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-desktop: 1.5rem
  margin: 1rem
  margin-tablet: 1.5rem
  margin-desktop: 2.5rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2.5rem
---

## Brand & Style

The platform addresses a dual-intent transactional ecosystem: hardware diagnostic repair and verified peer/dealer laptop commerce within Vietnam. The core brand personality is utilitarian, candid, and industrial. It trades marketing hyperbole for the transparency of an engineer's workbench.

The aesthetic fuses **Swiss utilitarianism** with **high-contrast industrial minimalism**:
- Absolute reliance on crisp structural grids, precise hairline dividers, and stark monochromatic contrast.
- Visual rhythm driven exclusively by typographic hierarchy and functional color accents.
- Zero ornamental skeuomorphism, zero gradient fills, zero drop shadows, and an absolute exclusion of decorative emojis or low-effort illustrative assets.
- Explicit emphasis on verified diagnostics, technical inspection metrics, and unadorned hardware specifications.

## Colors

The system employs a strict, high-contrast light-mode palette engineered for legibility across variable ambient lighting conditions in workshops and marketplaces.

### Functional Application
- **Primary Black (`#0B0B0B`)**: Applied to all primary headlines, interface chrome, primary buttons, structural borders, and critical hardware data points.
- **Accent Orange (`#FF4A1C`)**: Used exclusively for critical calls to action (e.g., "Đặt lịch sửa chữa", "Bán ngay"), key verification badges, diagnostic failure alerts, and high-priority states. Never used for passive decorative backgrounds.
- **Secondary Gray (`#8A8A86`)**: Secondary descriptions, hardware specs labels, inactive metadata, and auxiliary captions. Maintains an accessible contrast ratio against `#FFFFFF` and `#F4F4F2`.
- **Border (`#DADAD6`)**: Hairline 1px structural grid dividers, card perimeters, input field strokes, and data table rules.
- **Off-White (`#F4F4F2`)**: Canvas base tint, structural table header fills, secondary button backgrounds, and container backdrops.
- **White (`#FFFFFF`)**: Card surfaces, active input surfaces, modal containers, and elevated focal areas.

## Typography

Inter is applied uniformly across the entire interface without exception. No secondary display font family is permitted.

### Typesetting Rules
- **Vietnamese Diacritics**: Inter possesses native OpenType features for localized Vietnamese marks. Maintain generous line heights (`1.65` for body, `1.4` for technical data) to completely eliminate diacritic clipping or visual collision between consecutive lines.
- **Numbers and Currency**: Render all monetary sums (VND) with tabular numeral styling (`font-feature-settings: "tnum"`), ensuring prices in comparative diagnostic tables and marketplace grids align vertically without jitter.
- **Case Conventions**: Labels and metadata must remain in title or sentence case. Avoid forced uppercase text except for strict technical acronyms (RAM, SSD, GPU, CPU).

## Layout & Spacing

The structural framework is built upon a fixed 8pt architectural rhythm, utilizing a structured column grid that mimics modular hardware schematics.

### Grid Framework
- **Desktop (>= 1200px)**: 12-column layout. Max container width: `1280px`. Gutter: `1.5rem` (`24px`). Outer margin: `2.5rem` (`40px`).
- **Tablet (768px - 1199px)**: 8-column layout. Fluid container width. Gutter: `1rem` (`16px`). Outer margin: `1.5rem` (`24px`).
- **Mobile (< 768px)**: 4-column layout. Fluid container width. Gutter: `1rem` (`16px`). Outer margin: `1rem` (`16px`).

### Layout Philosophy
- **Modular Division**: Group content panels via explicit 1px lines using `#DADAD6` rather than arbitrary whitespace gaps.
- **Density**: High data density is favored over excessive negative space. Repair logs, parts inspection lists, and marketplace specs prioritize scanability and concise spatial pacing.

## Elevation & Depth

This design system rejects all drop shadows, blurred light projections, ambient diffusions, and skew angles. Visual hierarchy is communicated entirely through **planar layering, borders, and contrast shifts**.

### Stacking Tiers
1. **Canvas Base (Tier 0)**: `#F4F4F2`. The structural background floor across all full-width page layouts.
2. **Structural Paneling (Tier 1)**: `#FFFFFF`. The primary surface for cards, listing items, inspection reports, and forms. Outlined with a continuous 1px `#DADAD6` border.
3. **Interactive & Hover Focus (Tier 2)**: Hovered items shift border color from `#DADAD6` to `#0B0B0B`. The inner surface may toggle from `#FFFFFF` to `#F4F4F2` or remain stark white.
4. **Modals & Overlays (Tier 3)**: Modals sit atop a solid `#0B0B0B` backdrop with `40%` opacity (no blur filter). Modal containers use `#FFFFFF` fill, bounded by a mandatory 1px `#0B0B0B` border.

## Shapes

The interface adheres to an engineered, machined silhouette:
- All interactive controls, cards, input fields, badges, and modals have a strict border radius between `0px` and `4px`.
- Pills, stadium shapes, and exaggerated organic circular radii are strictly forbidden.
- Corner geometry tokens:
  - `radius-none`: `0px` (used for data tables, tabs, split button segments, full-bleed listing rows).
  - `radius-sm`: `2px` (used for chips, tags, verification badges, checkboxes).
  - `radius-default`: `4px` (maximum allowable radius; used for standalone cards, buttons, input fields, and modal containers).

## Components

### Buttons
- **Geometry**: Height `44px` (Desktop) / `40px` (Compact). Border-radius `4px` or `0px`. Padding: `0 1.25rem`. Typography: `15px`, weight `600`.
- **Primary**: Background `#0B0B0B`, text `#FFFFFF`, border `1px solid #0B0B0B`. Hover: Background `#262626`.
- **Accent (Call-to-Action)**: Background `#FF4A1C`, text `#FFFFFF`, border `1px solid #FF4A1C`. Hover: Background `#E03E15`.
- **Secondary / Ghost**: Background `#FFFFFF`, text `#0B0B0B`, border `1px solid #DADAD6`. Hover: Border `#0B0B0B`, background `#F4F4F2`.
- **Destructive**: Background `#FFFFFF`, text `#FF4A1C`, border `1px solid #FF4A1C`.

### Cards (Product Listing & Repair Tickets)
- **Base Style**: Background `#FFFFFF`, border `1px solid #DADAD6`, border-radius `4px`. Zero shadow.
- **Card Header**: Displays product title (`17px`, weight `600`) and verification status.
- **Body**: Secondary gray metadata (`14px`) detailing CPU, GPU, RAM, battery cycles, and repair history.
- **Price Block**: Product prices set at `22px`, weight `700`, color `#0B0B0B` (or `#FF4A1C` for promotional/urgent sale flags).
- **Hover Behavior**: Transition border from `#DADAD6` to `#0B0B0B` (duration `150ms`, ease `linear`). No elevation transform or scale change.

### Form Inputs & Selects
- **Labels**: Typography `15px`, weight `600`, color `#0B0B0B`. Positioned outside input with `0.5rem` bottom margin.
- **Fields**: Height `44px`. Background `#FFFFFF`, border `1px solid #DADAD6`, border-radius `2px` or `4px`. Typography: `16px`, weight `400`, color `#0B0B0B`. Placeholder color: `#8A8A86`.
- **Focus State**: Border color `#0B0B0B` (1px or 2px solid), outline: none.
- **Error State**: Border color `#FF4A1C`. Error message rendered below field in `13px`, weight `500`, color `#FF4A1C`.

### Checkboxes & Radio Buttons
- **Geometry**: Checkbox `18px x 18px` with `2px` radius. Radio `18px x 18px` circular (`radius: 50%` as exception for radio standard).
- **Default**: Background `#FFFFFF`, border `1px solid #DADAD6`.
- **Selected**: Background `#0B0B0B`, border `1px solid #0B0B0B`, checkmark/bullet `#FFFFFF`.

### Chips & Badges
- **Hardware Spec Chips**: Height `26px`, padding `0 8px`, border `1px solid #DADAD6`, background `#F4F4F2`, typography `13px`, weight `500`, color `#0B0B0B`.
- **Status Badges (Verified / Đã kiểm định)**: Height `24px`, padding `0 6px`, border `1px solid #0B0B0B`, background `#0B0B0B`, text `#FFFFFF`, typography `13px`, weight `600`.
- **Fault/Diagnostic Flags**: Height `24px`, padding `0 6px`, border `1px solid #FF4A1C`, background `#FFFFFF`, text `#FF4A1C`, typography `13px`, weight `600`.

### Diagnostic & Inspection Matrix Tables
- **Header**: Height `38px`, background `#F4F4F2`, border-bottom `1px solid #DADAD6`. Typography `13px`, weight `600`, color `#8A8A86`, uppercase acronyms.
- **Rows**: Alternating or single `#FFFFFF` rows with `1px solid #DADAD6` horizontal dividers. Cell padding: `12px 16px`. Data text: `14px`, weight `400` / `600`.