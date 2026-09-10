# HellMCLogin 1.0.0

Plugin de autenticacao para Paper 1.21.x.

- `/register <senha> <senha>`
- `/login <senha>`
- `/logout`
- auto-login quando o servidor esta em `online-mode=true`
- SQLite
- PBKDF2-HMAC-SHA256 + salt aleatorio
- bloqueio de movimento/interacao/chat antes do login
- `/helllogin info`, `/helllogin reload`, `/helllogin unregister <jogador>`

**Importante:** o auto-login oficial usa a autenticacao nativa do servidor. O plugin nao coleta senha, token ou credenciais Microsoft.

Compile com `mvn package`. O JAR sera gerado em `target/HellMCLogin-1.0.0.jar`.
