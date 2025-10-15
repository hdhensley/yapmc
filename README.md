# YAPMC

A privacy-focused, desktop API client built with Java Swing. Test REST APIs, manage environments, and organize your API calls - all stored locally on your machine.

![YAPMC Screenshot](example-screenshot.png)

## ✨ Features

- **🔒 Privacy First**: All data stored locally - no cloud, no tracking, no accounts
- **🌍 Environment Management**: Easily switch between Dev, Staging, Production, etc.
- **💾 Persistent Storage**: API calls and environments saved locally as JSON
- **📦 HAR Import**: Import API calls directly from browser HAR files
- **🎨 Customizable Themes**: Choose from multiple FlatLaf themes to personalize your workspace
- **⚙️ Configurable Storage**: Set custom storage locations for your JSON files
- **🔄 Variable Substitution**: Use environment variables in URLs, headers, and body
- **📊 Response Viewer**: Formatted JSON responses with status codes and timing
- **🚀 Modern UI**: Clean, responsive interface built with FlatLaf

## 🛠️ Requirements

### System Requirements

- **Java**: JDK 17 or higher (JDK 25 recommended for building)
  - Check your Java version: `java -version`
  - Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)

- **Maven**: Version 3.6 or higher (only needed for building from source)
  - Used for dependency management and building the project
  - Check your Maven version: `mvn -version`

### Dependencies

All dependencies are automatically managed by Maven. The project uses:

- **FlatLaf** (3.6.2): Modern, flat look and feel for Swing applications
  - Provides a contemporary UI that works across all platforms
  
- **FlatLaf IntelliJ Themes** (3.6.2): Additional theme options
  - Includes popular themes like Darcula and IntelliJ Light
  
- **GSON** (2.10.1): JSON parsing and serialization
  - Used for reading/writing configuration files and parsing JSON responses
  
- **Java Swing**: Built-in Java GUI framework (no external dependency)
  - Cross-platform desktop application framework

## 🚀 Getting Started

### Installation

#### Option 1: Download Pre-built Installers (Recommended)

Download the latest release for your operating system from the [Releases](https://github.com/hdhensley/yapmc/releases) page:

- **Windows**: `YAPMC-1.0.exe` - Double-click to install
- **macOS**: `YAPMC-1.0.dmg` - Open and drag to Applications folder
- **Linux**: `yapmc_1.0-1_amd64.deb` - Install with `sudo dpkg -i yapmc_1.0-1_amd64.deb`

The installers include a custom JRE, so you don't need Java installed on your system!

#### Option 2: Run the JAR File

If you have Java 17+ installed, you can download and run the JAR directly:

```bash
java -jar yapmc-1.0-SNAPSHOT.jar
```

#### Option 3: Build from Source

1. **Clone the repository:**
   ```bash
   git clone https://github.com/hdhensley/yapmc.git
   cd yapmc
   ```

2. **Build the project:**
   ```bash
   ./mvnw clean package
   ```
   
   On Windows, use:
   ```cmd
   mvnw.cmd clean package
   ```

3. **Run the application:**
   ```bash
   ./mvnw exec:java
   ```
   
   Or run the JAR directly:
   ```bash
   java -jar target/yapmc-1.0-SNAPSHOT.jar
   ```

### Quick Start Guide

1. **Select an Environment**: Choose from the dropdown in the top-right (Development, Staging, etc.)

2. **Configure Environment Variables**:
   - Click the "Manage" button
   - Add key-value pairs (e.g., `baseUrl` = `https://api.example.com`)
   - Click "Save"

3. **Create an API Call**:
   - Enter a name for your call
   - Enter the URL (use `{{variableName}}` for environment variables)
   - Select the HTTP method
   - Add headers (e.g., `Authorization: Bearer {{token}}`)
   - Add body parameters if needed
   - Click "Call" to execute or "Save" to store for later

4. **Import from HAR**:
   - Click the "Import" button in the Saved Calls panel
   - Select a `.har` file exported from your browser's Developer Tools
   - Choose which request to import
   - The call will be saved with the filename as its name

5. **Customize Settings**:
   - Click the "Settings" button in the top-right
   - Choose your preferred theme from the dropdown
   - Optionally set a custom storage location for your JSON files
   - Click "Save" to apply changes

## 📁 Project Structure

```
yapmc/
├── src/
│   ├── main/
│   │   ├── java/com/overzealouspelican/
│   │   │   ├── Main.java                      # Application entry point
│   │   │   ├── component/                     # Reusable UI components
│   │   │   │   ├── KeyValueInputGroup.java    # Headers/Body input component
│   │   │   │   ├── LabeledTextField.java      # Labeled text input
│   │   │   │   └── UrlWithMethodInput.java    # URL + HTTP method selector
│   │   │   ├── frame/                         # Dialog windows
│   │   │   │   ├── CallOutputFrame.java       # Response display window
│   │   │   │   ├── EnvironmentFrame.java      # Environment management
│   │   │   │   ��── ImportFrame.java           # HAR file import
│   │   │   ├── model/                         # Data models
│   │   │   │   ├── ApiCall.java               # API call configuration
│   │   │   │   ├── ApplicationState.java      # Global app state
│   │   │   │   └── Environment.java           # Environment with variables
│   │   │   ├── panel/                         # Main UI panels
│   │   │   │   ├── CallConfigurationPanel.java # Main form
│   │   │   │   ├── ControlPanel.java          # Top control bar
│   │   │   │   ├── MainContentPanel.java      # Content layout
│   │   │   │   ├── StatusPanel.java           # Bottom status bar
│   │   │   │   └── UrlPanel.java              # Saved calls sidebar
│   │   │   ├── service/                       # Business logic
│   │   │   │   ├── ApiCallService.java        # HTTP execution & persistence
│   │   │   │   └── EnvironmentService.java    # Environment persistence
│   │   │   └── util/                          # Utilities
│   │   │       ├── FontUtils.java             # Font management
│   │   │       └── IconUtils.java             # Icon loading
│   │   └── resources/
│   │       └── icons/                         # Application icons
│   └── test/                                   # Unit tests (future)
├── pom.xml                                     # Maven configuration
├── .gitignore                                  # Git ignore rules
└── README.md                                   # This file
```

## 🔧 Configuration

### Settings

Access application settings by clicking the "Settings" button in the control panel.

#### Theme Selection

Choose from four built-in themes:
- **FlatLaf Light**: Clean, bright interface (default)
- **FlatLaf Dark**: Easy on the eyes for extended use
- **FlatLaf IntelliJ**: Familiar IntelliJ IDEA light theme
- **FlatLaf Darcula**: Popular dark theme from IntelliJ

Themes are applied immediately and saved for future sessions.

#### Storage Location

By default, YAPMC stores data in OS-specific locations (see below). You can customize this:

1. Click "Settings" → "Browse..." to select a custom directory
2. Leave blank to use the default location
3. The app will offer to create the directory if it doesn't exist
4. Restart the application for the new location to take effect

**Note**: Changing storage location does not migrate existing data. You'll need to manually copy `environments.json` and `api-calls.json` to the new location if desired.

#### Reset Settings

Use the "Reset to Defaults" button to restore:
- Default theme (FlatLaf Light)
- Default storage location

### Data Storage Locations

**Default locations (when no custom location is set):**

- **macOS**: `~/Library/Application Support/YAPMC/`
- **Linux**: `~/.yapmc/`
- **Windows**: `%APPDATA%\YAPMC\`

**Files stored:**
- `environments.json` - Environment configurations with variables
- `api-calls.json` - Saved API call configurations

**Settings persistence:**
- Application settings (theme, storage location) are stored using Java Preferences API
- These settings are separate from your API call data

## 🎨 User Interface

The application follows SOLID principles with a modular design:

- **Control Panel**: Environment selection, manage environments, and application settings
- **URL Information Panel**: Main form for configuring API calls
- **Saved Calls Sidebar**: Quick access to saved API calls with import functionality
- **Status Bar**: Real-time status updates
- **Call Output Window**: Detailed response viewer with JSON formatting
- **Settings Window**: Theme selection and storage location configuration

## 🏗️ Building Installers

The project uses GitHub Actions with `jlink` and `jpackage` to create native installers for all platforms.

### Automated Builds (via GitHub Actions)

To trigger a release build:

1. Tag your commit: `git tag v1.0.0`
2. Push the tag: `git push origin v1.0.0`
3. GitHub Actions will automatically build installers for Windows, macOS, and Linux
4. Installers will be attached to the GitHub Release

### Manual Local Build

To create an installer on your local machine:

```bash
# Build the project
mvn clean package

# Create a custom JRE with jlink
jlink \
  --add-modules java.base,java.desktop,java.prefs,java.logging,java.net.http,java.naming \
  --output target/runtime \
  --strip-debug \
  --no-man-pages \
  --no-header-files \
  --compress=2

# Create the installer with jpackage
jpackage \
  --runtime-image target/runtime \
  --input target \
  --name YAPMC \
  --main-jar yapmc-1.0-SNAPSHOT.jar \
  --main-class com.overzealouspelican.Main \
  --type dmg \
  --app-version 1.0 \
  --vendor "YAPMC" \
  --description "Privacy-focused desktop API client"
```

Replace `--type dmg` with:
- `exe` for Windows
- `deb` or `rpm` for Linux
- `dmg` or `pkg` for macOS

### Benefits of jlink + jpackage

- **Self-contained**: Installers include a custom JRE (no Java installation required)
- **Small size**: jlink creates a minimal runtime (~50-70% smaller than full JDK)
- **Native experience**: Users get familiar installation process for their OS
- **Automatic updates**: Easy to distribute new versions

## 🔐 Security Note

Since all data is stored locally in plain text JSON files, be cautious about storing sensitive information like API keys or tokens directly in environment variables. Consider using temporary environment variables for sensitive data or implementing additional encryption if needed.

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

### What does this mean?

- ✅ Commercial use
- ✅ Modification
- ✅ Distribution
- ✅ Private use
- ❌ Liability
- ❌ Warranty

## 🤝 Contributing

Contributions are welcome! This is a FOSS (Free and Open Source Software) project built for the community.

### How to Contribute

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Guidelines

- Follow the existing code style and architecture patterns
- Test your changes thoroughly
- Update documentation as needed
- Keep the privacy-first principle in mind

## 📧 Contact

- **Issues**: Please use [GitHub Issues](https://github.com/yourusername/yapmc/issues) for bug reports and feature requests
- **Discussions**: Join the conversation in [GitHub Discussions](https://github.com/yourusername/yapmc/discussions)

---

**Built with privacy in mind. Your data stays yours.**
