<script lang="ts" setup>
import { ref, computed, defineProps, onMounted } from 'vue'
import dayjs from 'dayjs'
import { useI18n } from 'vue-i18n'
import CalendarIcon from '@@/components/Image/CalendarIcon.vue'
import CalendarArrow from '@@/components/Image/CalendarArrow.vue'

interface CalendarItem {
  count: string
  value: number | string
}

const props = defineProps<{ userFirstLogin: string }>()

const { t, locale } = useI18n()

const now = dayjs()
const nowTime = ref(now.format('YYYY-MM-DD'))
const clickItem = ref('')
const clickCount = ref(0)
const startTime = ref('')
const endTime = ref('')
const year = ref(now.year())
const month = ref(now.month() + 1)
const calendarList = ref<CalendarItem[]>([])

const dataTime = computed(() => {
  const m = formatMonth(month.value)
  return locale.value === 'en' ? `${m} ${year.value}` : `${year.value}年${m}`
})

const formatMonth = (m: number) => {
  const key = [
    'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
    'Jul', 'Aug', 'Sept', 'Oct', 'Nov', 'Dec'
  ][m - 1]
  return t(`calendar.month.${key}`)
}

const formatterDate = (d: string) => {
  if (!d) return ''
  const dt = dayjs(d)
  return locale.value === 'en'
    ? `${formatMonth(dt.month() + 1)} ${dt.date()}, ${dt.year()}`
    : `${dt.year()}年${formatMonth(dt.month() + 1)}${dt.date()}`
}

const drawCalendar = () => {
  const calendar: CalendarItem[] = []
  const firstDay = dayjs(`${year.value}-${month.value}-01`).day()
  for (let i = 0; i < firstDay; i++) {
    calendar.push({ count: '', value: '' })
  }
  const daysInMonth = dayjs(`${year.value}-${month.value}`).daysInMonth()
  for (let i = 1; i <= daysInMonth; i++) {
    const day = dayjs(`${year.value}-${month.value}-${i}`)
    calendar.push({
      value: i,
      count: day.format('YYYY-MM-DD')
    })
  }
  calendarList.value = calendar
}

const prevMonth = () => {
  if (month.value === 1) {
    month.value = 12
    year.value--
  } else {
    month.value--
  }
  drawCalendar()
}

const nextMonth = () => {
  if (month.value === 12) {
    month.value = 1
    year.value++
  } else {
    month.value++
  }
  drawCalendar()
}

const clickDate = (date: string) => {
  clickCount.value++
  clickItem.value = date
  if (clickCount.value % 2 === 1) {
    startTime.value = date
    endTime.value = ''
  } else {
    if (dayjs(date).isBefore(startTime.value)) {
      endTime.value = startTime.value
      startTime.value = date
    } else {
      endTime.value = date
    }
  }
}

const emit = defineEmits<{
  (e: 'checkedDate', val: string[]): void
}>()

const confirm = () => {
  if (!startTime.value || !endTime.value) return
  emit('checkedDate', [startTime.value, endTime.value, '1'])
}

onMounted(() => {
  drawCalendar()
})
</script>

<template>
  <div class="calendar-wrap" @click.stop>
    <div class="calendar">
      <div class="top">
        <CalendarIcon />
        <span v-if="startTime && endTime">{{ formatterDate(startTime) }} - {{ formatterDate(endTime) }}</span>
      </div>

      <div class="title">
        <div class="btn btn-left" @click.stop="prevMonth">
          <CalendarArrow />
        </div>
        <div class="text">{{ dataTime }}</div>
        <div class="btn btn-right" @click.stop="nextMonth">
          <CalendarArrow />
        </div>
      </div>

      <div class="head">
        <div v-for="(day, i) in [t('calendar.week.Sun'), t('calendar.week.Mon'), t('calendar.week.Tues'), t('calendar.week.Wed'), t('calendar.week.Thur'), t('calendar.week.Fri'), t('calendar.week.Sat')]" :key="i" class="days">
          {{ day }}
        </div>
      </div>

      <div class="wrap">
        <div
          class="wrap-box"
          v-for="(item, index) in calendarList"
          :key="index"
          :class="[
            (index + 1) % 7 === 0 ? 'saturday' : '',
            item.count >= startTime && item.count < endTime ? 'in-range' : ''
          ]">

          <div
            class="span"
            @click.stop="clickDate(item.count)"
            :class="{
              empty: item.count === '',
              disabled: item.count < dayjs(props.userFirstLogin).format('YYYY-MM-DD') || item.count > nowTime,
              active: item.count === startTime || item.count === endTime,
              today: item.count === nowTime
            }">
            {{ item.value }}
          </div>
        </div>
      </div>

      <div class="bottom-btn">
        <div class="sure-btn" :class="{ disabled: !startTime || !endTime }" @click.stop="confirm">
          {{ t('calendar.customDate') }}
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.calendar-wrap {
  position: absolute;
  text-align: center;
  background: #FFFFFF;
  box-shadow: 0px 4px 35px rgba(129, 149, 200, 0.18);
  border-radius: 16px;
  padding: 24px 0;
  cursor: auto;
  z-index: 3;
  .calendar {
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
      .span {
        background: transparent;
        &.active{
          color: #fff;
          background: #1460F3;
        }
      }
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
  margin-top: 16px;
  text-align: right;
  .sure-btn {
    padding: 6px 12px;
    margin-right: 24px;
    border-radius: 6px;
    background: #396FFA;
    color: #fff;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    width: fit-content;
    margin-left: auto;
    &:hover {
      background-color: #244FF0;
    }
    &.disabled {
      cursor: not-allowed;
      pointer-events: none;
      opacity: 0.3;
      &:hover {
        background-color: #396FFA;
      }
    }
  }
}
@media screen and (max-width: 1235px) {
  .calendar-wrap {
    left: unset;
    right: 0;
  }
}
@media screen and (max-width: 767px) {
  .calendar-wrap {
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
    .calendar {
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
