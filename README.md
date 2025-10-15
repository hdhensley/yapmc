# YAPMC - Yet Another Postman Clone

A lightweight, privacy-focused API testing tool built with Java Swing. YAPMC provides a clean interface for making HTTP requests, managing environments, and organizing API calls - all while keeping your data completely local and private.

## 🔒 Privacy First

**All data is stored locally on your machine.** YAPMC does not transmit, upload, or share any of your API calls, environment variables, headers, or response data with external services. Everything stays on your computer.

- Environment variables are saved to: `~/Library/Application Support/YAPMC/` (macOS) or `~/.yapmc/` (Linux) or `%APPDATA%\YAPMC\` (Windows)
- API calls are saved to: `~/Library/Application Support/YAPMC/api-calls.json`
- No telemetry, no analytics, no external connections (except the API calls you explicitly make)

## ✨ Features

- **HTTP Request Testing**: Support for GET, POST, PUT, DELETE, PATCH, HEAD, and OPTIONS methods
- **Environment Management**: Create and manage multiple environments (Development, Staging, Production, etc.)
- **Environment Variables**: Use `{{variableName}}` placeholders in URLs, headers, and body parameters
- **Request Organization**: Save and organize your API calls for easy reuse
- **HAR File Import**: Import API calls from browser HAR (HTTP Archive) files
- **Response Formatting**: Automatic JSON pretty-printing for responses
- **Headers & Body**: Flexible key-value input for headers and request body parameters
- **Call History**: View detailed output including request details, environment variables, and responses

## 🛠️ Technology Stack

### Required Software

- **Java**: Version 11 or higher (tested with Java 25)
  - The application uses the modern `HttpClient` API introduced in Java 11
  - Check your Java version: `java -version`
  
- **Maven**: Version 3.6 or higher
  - Used for dependency management and building the project
  - Check your Maven version: `mvn -version`

### Dependencies

All dependencies are automatically managed by Maven. The project uses:

- **FlatLaf** (3.2.5): Modern, flat look and feel for Swing applications
  - Provides a contemporary UI that works across all platforms
  
- **GSON** (2.10.1): JSON parsing and serialization
  - Used for reading/writing configuration files and parsing JSON responses
  
- **Java Swing**: Built-in Java GUI framework (no external dependency)
  - Cross-platform desktop application framework

## 🚀 Getting Started

### Installation

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd yapmc
   ```

2. **Build the project:**
   ```bash
   ./mvnw clean compile
   ```
   
   On Windows, use:
   ```cmd
   mvnw.cmd clean compile
   ```

3. **Run the application:**
   ```bash
   ./mvnw exec:java -Dexec.mainClass="com.overzealouspelican.Main"
   ```
   
   Or package and run as a JAR:
   ```bash
   ./mvnw clean package
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
│   │   │   │   └── ImportFrame.java           # HAR file import
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

### Data Storage Locations

- **macOS**: `~/Library/Application Support/YAPMC/`
- **Linux**: `~/.yapmc/`
- **Windows**: `%APPDATA%\YAPMC\`

Files stored:
- `environments.json` - Environment configurations with variables
- `api-calls.json` - Saved API call configurations

### Environment Variables

Use double curly braces to reference environment variables in:
- URLs: `{{baseUrl}}/api/v1/users`
- Headers: `Authorization: Bearer {{apiToken}}`
- Body parameters: `user_id: {{userId}}`

Variables are substituted at runtime when you execute a call.

## 🎨 User Interface

The application follows SOLID principles with a modular design:

- **Control Panel**: Environment selection and management
- **URL Information Panel**: Main form for configuring API calls
- **Saved Calls Sidebar**: Quick access to saved API calls
- **Status Bar**: Real-time status updates
- **Call Output Window**: Detailed response viewer with JSON formatting

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
