---
name: Wallixa
colors:
  surface: '#faf8ff'
  surface-dim: '#dbd9e0'
  surface-bright: '#faf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f4f3fa'
  surface-container: '#efedf4'
  surface-container-high: '#e9e7ee'
  surface-container-highest: '#e3e1e8'
  on-surface: '#1a1b20'
  on-surface-variant: '#4a4455'
  inverse-surface: '#2f3035'
  inverse-on-surface: '#f2f0f7'
  outline: '#7b7487'
  outline-variant: '#ccc3d8'
  surface-tint: '#732ee4'
  primary: '#630ed4'
  on-primary: '#ffffff'
  primary-container: '#7c3aed'
  on-primary-container: '#ede0ff'
  inverse-primary: '#d2bbff'
  secondary: '#5f5a7c'
  on-secondary: '#ffffff'
  secondary-container: '#dcd5fd'
  on-secondary-container: '#605b7d'
  tertiary: '#6316cf'
  on-tertiary: '#ffffff'
  tertiary-container: '#7c3de8'
  on-tertiary-container: '#ede1ff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#eaddff'
  primary-fixed-dim: '#d2bbff'
  on-primary-fixed: '#25005a'
  on-primary-fixed-variant: '#5a00c6'
  secondary-fixed: '#e5deff'
  secondary-fixed-dim: '#c8c2e9'
  on-secondary-fixed: '#1b1735'
  on-secondary-fixed-variant: '#474363'
  tertiary-fixed: '#ebddff'
  tertiary-fixed-dim: '#d3bbff'
  on-tertiary-fixed: '#250059'
  on-tertiary-fixed-variant: '#5b00c5'
  background: '#faf8ff'
  on-background: '#1a1b20'
  surface-variant: '#e3e1e8'
typography:
  headline-xl:
    fontFamily: Plus Jakarta Sans
    fontSize: 40px
    fontWeight: '800'
    lineHeight: 48px
    letterSpacing: -0.03em
  headline-xl-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 30px
    fontWeight: '800'
    lineHeight: 36px
    letterSpacing: -0.025em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 34px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 28px
    letterSpacing: -0.015em
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: -0.01em
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: -0.005em
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Inter
    fontSize: 10px
    fontWeight: '700'
    lineHeight: 14px
    letterSpacing: 0.04em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 0.75rem
  gutter-md: 1rem
  margin: 1rem
  margin-md: 1.5rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1rem
  space-xl: 1.5rem
---

## Brand & Style
The design system positions the product as an expressive, high-curation wallpaper client crafted specifically for an aesthetic-driven Gen Z audience. The visual language balances high-energy contemporary youth culture with the disciplined precision of modern native Android design (Material You / modern Scandinavian UI hybrid). 

The emotional tone is inspiring, airy, and tactile. Instead of cluttering the viewport with heavy chrome, the interface acts as a silent gallery frame that honors visual artwork. Key traits include:
- **Clean Expressionism:** Vibrant ultraviolet accents contrast against airy, lavender-infused paper tones.
- **Micro-tactility:** Generously rounded corners (16px–24px), smooth pill indicators, and floating navigation chrome give an organic, thumb-friendly feel.
- **Edge-to-Edge Fluidity:** Dynamic transitions respect Android system insets, full-bleed imagery, and translucent system navigation bars.

## Colors
The palette leverages chromatic contrast between deep royal violets and ultra-soft, tinted whites. 

- **Primary Violet (`#7C3AED`) & Deep Royal (`#6D28D9`):** Reserved for primary interactive touchpoints, high-emphasis action triggers, active tab indicators, and dynamic brand moments.
- **Accent Lavender (`#DDD6FE`):** Deployed for active pill backgrounds, chip fills, soft badge states, and visual focus rings.
- **Canvas (`#FAF8FF` to `#F5F3FF`):** A soft, ambient off-white base that eliminates sterile clinical gray while maintaining pristine lightness.
- **Surfaces (`#FFFFFF`):** High-tier cards and modal overlays use pure white to cleanly float over the subtle tinted canvas.
- **Typography Tiers:**
  - High-contrast ink (`#111827`) provides crisp, punchy legibility on titles.
  - Subdued slate (`#6B7280`) prevents visual fatigue across metadata, photographer credits, and secondary specifications.
  - Ghost border stroke (`rgba(124, 58, 237, 0.08)`) maintains structural crispness without harsh separation.

## Typography
Plus Jakarta Sans serves as the primary voice across headlines and body copy, bringing youthful geometry and open aperture curves that reflect modern Gen Z aesthetics. Inter provides precision and neutral legibility on UI microcopy, counters, labels, and technical wallpaper resolution specs.

- **Headlines:** Set tight with negative tracking to convey punchy editorial impact.
- **Numbers & Resolution Tags:** Inter ensures tabular alignment and balanced metrics when presenting ratios (e.g., `4K`, `9:16`, `120Hz`).
- **Scale Behavior:** Mobile viewport headings clamp at `headline-xl-mobile` to avoid line-wrapping on single-word mood categories (e.g., "Aesthetic", "Cyberpunk", "Minimalist").

## Layout & Spacing
The layout follows a fluid-first mobile approach tailored for edge-to-edge Android viewport displays (accounting for top camera cutouts and gesture navigation bars).

- **Grid Model:** 
  - Mobile screens default to a 2-column masonry or aspect-ratio grid (`9:19.5` phone proportion preview cards) using a `0.75rem` (`12px`) gutter and `1rem` (`16px`) outer margins.
  - Large phone and tablet viewports scale to a 3 or 4-column adaptive layout with `1rem` gutters and `1.5rem` margins.
- **Vertical Rhythm:**
  - Compact `space-xs` (4px) and `space-sm` (8px) gaps bind related elements (e.g., creator avatar next to title).
  - Component cards and curated mood rails maintain `space-lg` (16px) separation to give wallpapers breathing room.
  - Section dividers use `space-xl` (24px) spacing without relying on physical horizontal divider lines.

## Elevation & Depth
Depth relies on ambient, diffuse shadows combined with low-contrast tinted borders, creating an effortless levitation effect for white surfaces resting on the soft lavender canvas.

- **Level 0 (Canvas):** Base background tint (`#FAF8FF`), completely flat.
- **Level 1 (Cards & Tiles):** Pure white `#FFFFFF` cards with an ultra-soft violet-tinted shadow: `0 8px 24px -4px rgba(109, 40, 217, 0.06), 0 2px 6px -1px rgba(17, 24, 39, 0.04)`. Outlined with a hairline border: `1px solid rgba(124, 58, 237, 0.06)`.
- **Level 2 (Floating Action Pill / Modals):** High-elevation floating bottom navigation dock and floating quick-actions: `0 16px 36px -6px rgba(109, 40, 217, 0.16), 0 4px 12px -2px rgba(17, 24, 39, 0.06)`, framed with a frosted glass backdrop filter (`backdrop-filter: blur(16px)` at 88% surface opacity).
- **Scrim & Preview Mode:** Tap-to-inspect triggers a pure blackout background (`#000000`) with system status bar elements automatically switching to high-contrast white.

## Shapes
The design uses generous roundedness to convey a fluid, approachable, and distinctly modern mobile feel:
- **Small Controls (Buttons, Inputs, Badges):** Fully rounded pill geometry (`border-radius: 9999px`) or `12px` minimum radius.
- **Wallpaper Thumbnails & Standard Cards:** `rounded-2xl` (`16px`) for standard cards; `rounded-3xl` (`24px`) for hero collection headers and preview canvases.
- **Bottom Navigation Dock:** Contoured pill dock with `28px` corner radius, floating `16px` above the Android gesture bar.
- **Active Indicators:** Elongated capsules with continuous squircle corners.

## Components

### Buttons
- **Primary Action (e.g., 'Apply Wallpaper', 'Download 4K'):** Solid `#7C3AED` fill, white bold text (`label-lg`), pill-shaped (`rounded-full`), `48px` default height. On touch press, scales down smoothly to `97%` scale with color shifting to `#6D28D9`.
- **Secondary / Ghost Pill:** Background `#DDD6FE` at 30% opacity, text `#6D28D9`, zero border.
- **Floating Download Button:** Circular `56px × 56px` or wide floating pill (`160px × 48px`) elevated with Level 2 shadow.

### Floating Bottom Navigation Bar
- Floats detached from screen edges: `16px` bottom, `20px` horizontal padding.
- Pure white surface with subtle frosted glass transmission (`rgba(255, 255, 255, 0.9)` + `blur(20px)`).
- Active tab uses a dynamic horizontal lavender capsule (`#DDD6FE` at 60% opacity) enclosing the royal purple icon and label. Inactive tabs render in `#6B7280`.

### Wallpaper Cards & Masonry Tiles
- Fixed ratio options: `9:16` or `9:19.5` phone preview cards.
- Border radius: `20px` (`rounded-2xl`), overflow hidden, resting Level 1 shadow.
- Ambient overlay: Bottom gradient scrim (`rgba(0,0,0,0) 50%` to `rgba(0,0,0,0.45) 100%`) for clean white text overlay showing mood category or tag count.
- Favorite toggle: Floating circular blur pill (`36px × 36px`, `rgba(255,255,255,0.85)`) placed at the top-right corner.

### Chips & Filter Pills
- Fully pill-shaped (`9999px`), `36px` height.
- Unselected state: Pure white background, `1px solid rgba(124, 58, 237, 0.12)`, text `#111827`.
- Selected state: Primary purple `#7C3AED` fill with crisp white text, or accent lavender `#DDD6FE` with deep violet `#6D28D9` text for secondary filter sets.

### Input Fields & Search Bar
- `48px` height with `24px` corner radius.
- Background `#FAF8FF` inside modal surfaces, or `#FFFFFF` on canvas.
- Prefix search icon in `#7C3AED`. Placeholder text in `#6B7280`. Focused state adds an active `2px` focus ring in `#DDD6FE`.

### Checkboxes & Toggle Controls
- Radios and switches employ violet active states (`#7C3AED`) with smooth mechanical sliding animation on the pill thumb.