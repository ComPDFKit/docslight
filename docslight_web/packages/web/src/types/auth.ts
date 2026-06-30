export type ZeroOne = '0' | '1'

export interface AuthUser {
  access_token: string
  avatar: string | null
  color_schema: string
  create_date: string
  create_time: number
  email: string
  id: string
  is_active: ZeroOne
  is_anonymous: ZeroOne
  is_authenticated: ZeroOne
  is_superuser: boolean
  language: string
  last_login_time: string
  leader_id: string
  login_channel: string
  nickname: string
  role: string
  status: ZeroOne
  timezone: string
  update_date: string
  update_time: number
}
