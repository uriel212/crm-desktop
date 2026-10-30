# Instalar CRM de Ventas

La app se conecta a la API en la nube, así que necesita **internet**. No hace falta instalar Java: el instalador ya lo trae.

## 1. Descargar el instalador
Los instaladores los genera GitHub Actions (workflow `.github/workflows/instaladores.yml`).

1. Entra a https://github.com/uriel212/crm-desktop/actions (necesitas acceso al repositorio).
2. Abre **Instaladores** y la ejecución más reciente con ✅.
   - Para generar una nueva: **Run workflow** › **Run workflow** (tarda unos minutos).
3. En **Artifacts**, descarga:
   - **Windows:** `instalador-exe`
   - **macOS (Apple Silicon):** `instalador-dmg`
4. Descomprime el `.zip` descargado.

> Si tu compañero no tiene acceso al repo, descarga tú el instalador y pásaselo (USB, Drive, etc.).

## 2. Instalar en Windows
1. Doble clic en `CRM de Ventas-1.0.0.exe`.
2. Si aparece **"Windows protegió tu PC"**: clic en **Más información** › **Ejecutar de todas formas** (sale porque el instalador no está firmado).
3. Elige la carpeta y termina el asistente. No pide permisos de administrador.
4. Abre **CRM de Ventas** desde el menú Inicio o el acceso directo del escritorio.

Para desinstalar: **Configuración › Aplicaciones › CRM de Ventas › Desinstalar**.

## 3. Instalar en macOS
1. Abre `CRM de Ventas-1.0.0.dmg` y arrastra la app a **Aplicaciones**.
2. La primera vez, si dice que **no se puede abrir** (app no firmada): clic derecho sobre la app › **Abrir** › **Abrir**.
   - Si no aparece la opción: **Ajustes del Sistema › Privacidad y seguridad › Abrir de todas formas**.

## 4. Iniciar sesión
Usuarios de prueba (contraseña `Password123`):

| Email | Rol |
|---|---|
| `ana.torres@crmventas.test` | ADMIN |
| `luis.ramirez@crmventas.test` | GERENTE |
| `carla.mendoza@crmventas.test` | VENDEDOR |

La primera acción puede tardar unos segundos mientras el servidor en la nube se activa.

## Generar el instalador a mano en Windows (opcional)
Solo si no se quiere usar GitHub Actions. Requisitos: **JDK 25** ([Adoptium](https://adoptium.net)), **Git** y **WiX Toolset 3.14** ([wixtoolset.org](https://wixtoolset.org), agregado al `PATH`).

```bat
git clone https://github.com/uriel212/crm-desktop.git
cd crm-desktop
gradlew.bat installDist
jpackage --type exe --name "CRM de Ventas" --app-version 1.0.0 --vendor DevSUJ ^
  --input build\install\crm-desktop\lib --main-jar crm-desktop-0.0.1-SNAPSHOT.jar ^
  --main-class com.devsuj.crmdesktop.Launcher --java-options "--enable-native-access=ALL-UNNAMED" ^
  --win-menu --win-shortcut --win-dir-chooser --win-per-user-install --dest instalador
```
El instalador queda en `instalador\CRM de Ventas-1.0.0.exe`.
