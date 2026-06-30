import { defineConfig } from 'windicss/helpers'

export default defineConfig({
  darkMode: 'class', // or 'media'
  theme: {
    container: {
      center: true,
    },
    screens: {
      sm: '320px',
      tiny: '576px',
      md: '768px',
      lg: '930px',
      xl: '1024px',
      '2xl': '1280px',
      '3xl': '1536px'
    },
    extend: {
      colors: {
        brand: {
          /** 品牌色/主色 */
          0: '#232748',
          /** 品牌色/H1 */
          1: '#94969D',
          /** 品牌色/H2 */
          2: '#396FFA',
          /** 品牌色/H3 */
          3: '#52555F',
          /** 品牌色/H4 */
          4: '#AAAEB2',
          /** 品牌色/H5 */
          5: '#BABABA'
        }
      },
      lineHeight: {
        'snug': '1.33',
        'normal': '1.50',
        'relaxed': '1.56'
      },
      fontSize: {
        'xs': ['14px', '20px'],
        'sm': ['16px', '24px'],
        'tiny': ['20px', '28px'],
        'base': ['24px', '36px'],
        'lg': ['30px', '40px'],
        'xl': ['36px', '48px'],
        '2xl': ['50px', '64px']
      }
    }
  }
})
