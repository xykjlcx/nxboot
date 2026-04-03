/** 在线用户 */
export interface OnlineUserVO {
  sessionId: string;
  userId: number;
  username: string;
  ip: string;
  userAgent: string;
  loginTime: number;
}
