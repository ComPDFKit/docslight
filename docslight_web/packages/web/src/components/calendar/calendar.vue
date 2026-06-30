<template>
  <div class="calender-wrap" @click.stop>
    <div class="calender">
      <div class="top">
        <CalendarIcon />
        <span v-show="startTime !== '' && endTime !== ''">{{ formatterDate(startTime) + ' - ' + formatterDate(endTime) }}</span>
        <span v-show="startTime === '' || endTime === ''" class="placeholder">{{ t('extraction.selectDate') }}</span>
      </div>

      <div class="title">
        <div class="btn btn-left" @click.stop="last()"><CalendarArrow /></div>
        <div class="text">{{ dataTime }}</div>
        <div class="btn btn-right" @click.stop="next()"><CalendarArrow /></div>
      </div>

      <div class="head">
        <div class="days" v-for="(item, index) in weekList" :key="index">
          {{ item }}
        </div>
      </div>

      <div class="wrap">
        <div class="wrap-box"
            v-for="(item, index) in calendarList"
            :key="index"
            :class="[(index + 1) % 7 === 0 ? 'saturday' : '',
            item.count >= startTime && item.count < endTime ? 'in-range' : ''
          ]"
            >
          <div
            class="span"
            @click.stop="click(item.count)"
            :class="
              item.count == ''
                ? 'empty'
                : item.count < fillerDate(userFirstLogin) || item.count > nowTime
                ? 'disabled'
                : item.count == startTime || item.count == endTime
                ? 'active'
                : item.count >= startTime && item.count < endTime
                ? 'in-range'
                : item.count == nowTime
                ? 'today'
                : ''
                "
            >
            {{ item.value }}
          </div>
        </div>
      </div>

      <div class="bottom-btn">
        <button class="sure-btn" :class="{'disabled': startTime === '' || endTime === ''}" @click.stop="firm()">{{ t('extraction.customDate') }}</button>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'

interface CalendarItem {
  count: string
  value: number | string
}

// Props
const props = defineProps<{
  userFirstLogin: string
}>()

// Emits
const emit = defineEmits<{
  (e: 'checkedDate', dateArr: string[]): void
  (e: 'close'): void
}>()

// i18n
const { t, locale } = useI18n()

// Reactive state
const nowTime = ref('') // 当前日期的时间戳
const clickItem = ref('0') // 点击的时间戳
const clickCount = ref(0) // 点击次数
const startTime = ref('') // 开始时间
const endTime = ref('') // 结束时间
const weekList = ref<string[]>([
  t('calendar.week.Sun'),
  t('calendar.week.Mon'),
  t('calendar.week.Tues'),
  t('calendar.week.Wed'),
  t('calendar.week.Thur'),
  t('calendar.week.Fri'),
  t('calendar.week.Sat')
]) // 星期列表
const year = ref(new Date().getFullYear()) // 日历上的年份
const month = ref(new Date().getMonth() + 1) // 日历上的月份
const calendarList = ref<CalendarItem[]>([])

const nowYear = new Date().getFullYear()
const nowMonth = new Date().getMonth() + 1
const nowDay = new Date().getDate()

// Computed
const dataTime = computed(() => {
  return locale.value === 'en'
    ? `${formatMonth(month.value)} ${year.value}`
    : `${year.value}年${formatMonth(month.value)}`
})

// Methods
const draw = (y: number, m: number) => {
  const calendar: CalendarItem[] = []

  // 用当月第一天在一周中的日期值作为当月离第一天的天数(获取当月第一天是周几)
  const firstDay = new Date(y, m - 1, 1).getDay()
  for (let i = 1; i <= firstDay; i++) {
    calendar.push({ value: '', count: '' })
  }

  // 用当月最后一天在一个月中的日期值作为当月的天数
  const monthDay = new Date(y, m, 0).getDate()
  for (let i = 1; i <= monthDay; i++) {
    const timeMonth = m < 10 ? `0${m}` : `${m}`
    const timeDay = i < 10 ? `0${i}` : `${i}`
    calendar.push({
      value: i,
      count: `${y}-${timeMonth}-${timeDay}`
    })
  }

  calendarList.value = calendar
}

const last = () => {
  month.value--
  if (month.value === 0) {
    month.value = 12
    year.value--
  }
  draw(year.value, month.value)
}

const next = () => {
  month.value++
  if (month.value === 13) {
    month.value = 1
    year.value++
  }
  draw(year.value, month.value)
}

const click = (item: string) => {
  clickCount.value++
  clickItem.value = item

  // 开始日期
  if (clickCount.value % 2 === 1) {
    startTime.value = clickItem.value
    endTime.value = ''
  } else {
    endTime.value = clickItem.value
    if (startTime.value > endTime.value) {
      endTime.value = startTime.value
      startTime.value = clickItem.value
    }
  }
}

const firm = () => {
  const dateArr = [startTime.value, endTime.value]
  emit('checkedDate', dateArr)
  emit('close')
}

// 日期自定格式
const formatterDate = (data: string) => {
  if (!data) return ''
  const dt = new Date(data)
  const m = formatMonth(dt.getMonth() + 1)
  return locale.value === 'en'
    ? `${m} ${dt.getDate()}, ${dt.getFullYear()}`
    : `${dt.getFullYear()}年${m}${dt.getDate()}日`
}

// 格式化月份
const formatMonth = (m: number): string => {
  const monthKeys: Record<number, string> = {
    1: 'calendar.month.Jan',
    2: 'calendar.month.Feb',
    3: 'calendar.month.Mar',
    4: 'calendar.month.Apr',
    5: 'calendar.month.May',
    6: 'calendar.month.Jun',
    7: 'calendar.month.Jul',
    8: 'calendar.month.Aug',
    9: 'calendar.month.Sept',
    10: 'calendar.month.Oct',
    11: 'calendar.month.Nov',
    12: 'calendar.month.Dec'
  }
  return monthKeys[m] ? t(monthKeys[m]) : ''
}

const fillerDate = (val: string) => {
  return dayjs(val).format('YYYY-MM-DD')
}

// 初始化
draw(nowYear, nowMonth)
const timeMonth = nowMonth < 10 ? `0${nowMonth}` : `${nowMonth}`
const timeDay = nowDay < 10 ? `0${nowDay}` : `${nowDay}`
nowTime.value = `${nowYear}-${timeMonth}-${timeDay}`

</script>
<style scoped lang="scss">
.calender-wrap {
  position: absolute;
  top: 0px;
  right: 101%;
  text-align: center;
  background: #FFFFFF;
  box-shadow: 0px 4px 35px rgba(129, 149, 200, 0.18);
  border-radius: 16px;
  padding: 24px 0;
  cursor: auto;
  z-index: 3;
  .calender {
    display: inline-block;
    width: 392px;
  }
}
.wrap {
  width: calc(100% - 28px);
  margin: 0 28px;
  display: flex;
  flex-wrap: wrap;
  .wrap-box {
    margin-bottom: 2px;
    &.in-range {
      background: rgba(20, 96, 243, 0.1);
    }
    &.saturday > .span {
      margin-right: 0;
    }
  }
}

.top {
  padding-left: 24px;
  padding-bottom: 20px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #F4F8FF;
  cursor: default;
  span {
    margin-left: 12px;
    font-size: 18px;
    line-height: 28px;
    color: #43474D;
    &.placeholder {
      color: #AAAEB2;
    }
  }
}
.span {
  width: 26px;
  height: 26px;
  margin-right: 26px;
  background: #fff;
  color: #43474D;
  text-align: center;
  font-size: 14px;
  line-height: 26px;
  border-radius: 4px;
  cursor: pointer;
  &.today {
    border: 1px solid #1460F3;
  }
  &.active {
    background: #1460F3;
    color: #fff;
  }

  &.in-range {
    background: transparent;
  }

  &.empty {
    background: #fff;
    pointer-events: none;
  }

  &.disabled {
    color: #BABABA;
    pointer-events: none;
    background-color: #E8E8E8;
  }
}

.title {
  width: calc(100% - 46px);
  height: 24px;
  display: flex;
  flex-wrap: nowrap;
  text-align: center;
  color: #18191B;
  font-weight: bold;
  line-height: 24px;
  font-size: 20px;
  font-weight: 700;
  margin: 0 23px;
  border-bottom: 1px solid #F4F8FF;
  .btn {
    width: 1.2rem;
    cursor: pointer;
    &.disabled {
      pointer-events: none;
      background: #ccc;
    }
  }
  .btn-right {
    transform: rotate(180deg);
  }
  .text {
    flex: 1;
  }
}

.head {
  height: 40px;
  display: flex;
  margin: 0 26px;
  font-size: 14px;
  color: #43474D;
  font-weight: 700;
  margin-top: 38px;
  flex-wrap: nowrap;
  line-height: 40px;
  text-align: center;
  justify-content: space-between;
}

.bottom-btn {
  width: 100%;
  margin-top: 13px;
  text-align: right;
  .sure-btn {
    padding: 10px 17.5px;
    margin-right: 24px;
    border-radius: 6px;
    background: #1665FF;
    color: #fff;
    font-size: 14px;
    font-weight: 700;
    &.disabled {
      cursor: not-allowed;
      pointer-events: none;
      opacity: 0.3;
    }
  }
}
@media screen and (max-width: 1235px) {
  .calender-wrap {
    left: unset;
    right: 0;
  }
}
@media screen and (max-width: 767px) {
  .calender-wrap {
    position: fixed;
    left: 0;
    right: 0;
    top: unset;
    bottom: 0;
    margin: auto;
    box-shadow: none;
    border-bottom-left-radius: 0;
    border-bottom-right-radius: 0;
    width: 100%;
    background-color: #fff;
    .calender {
      width: 100%;
      max-width: 326px;
      .wrap .wrap-box {
        margin-bottom: 10px;
        .span {
          width: 30px;
          height: 30px;
          margin-right: 18px;
        }
        &.saturday .span {
          margin-right: 0;
        }
      }
    }
  }
  .top {
    height: 48px;
    padding-left: 7px;
  }
  .title {
    width: calc(100% - 14px);;
    margin: 0 7px;
  }
  .head {
    height: auto;
    margin: 15px 0;
    .days {
      width: 38px;
      line-height: 16px;
      height: 16px;
    }
  }
  .wrap {
    width: calc(100% - 8px);
    margin: 0 4px;
  }
  .bottom-btn {
    margin-top: 27px;
    .sure-btn {
      margin-left: 3px;
      margin-right: 3px;
      width: calc(100% - 6px);
      height: 40px;
      &.disabled {
        opacity: 0.3;
      }
    }
  }
}
</style>
