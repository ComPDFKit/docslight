import forge from 'node-forge'

export const rsaPsw = (password: string) => {
  const pub =
    '-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArq9XTUSeYr2+N1h3Afl/z8Dse/2yD0ZGrKwx+EEEcdsBLca9Ynmx3nIB5obmLlSfmskLpBo0UACBmB5rEjBp2Q2f3AG3Hjd4B+gNCG6BDaawuDlgANIhGnaTLrIqWrrcm4EMzJOnAOI1fgzJRsOOUEfaS318Eq9OVO3apEyCCt0lOQK6PuksduOjVxtltDav+guVAA068NrPYmRNabVKRNLJpL8w4D44sfth5RvZ3q9t+6RTArpEtc5sh5ChzvqPOzKGMXW83C95TxmXqpbK6olN4RevSfVjEAgCydH6HN6OhtOQEcnrU97r9H0iZOWwbw3pVrZiUkuRD1R56Wzs2wIDAQAB-----END PUBLIC KEY-----'

  // ------------------------------------------------------
  // 1. 导入公钥
  // ------------------------------------------------------
  const publicKey = forge.pki.publicKeyFromPem(pub)

  // ------------------------------------------------------
  // 2. 后端逻辑：先 base64(password)
  // ------------------------------------------------------
  const base64Password = forge.util.encode64(password)

  // ------------------------------------------------------
  // 3. RSA-OAEP + SHA-256 加密（关键）
  // ------------------------------------------------------
  const encryptedBytes = publicKey.encrypt(base64Password, 'RSA-OAEP', {
    md: forge.md.sha256.create()
  })

  // ------------------------------------------------------
  // 4. 再 base64 输出给后端
  // ------------------------------------------------------
  return forge.util.encode64(encryptedBytes)
}
