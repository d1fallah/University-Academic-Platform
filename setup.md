# Setup Guide for Development Environment

This guide provides PowerShell commands to set up Java 21.0.7 and the latest version of Maven on Windows.

## Java Installation

### 1. Create a directory for Java installation

```powershell
# Create directory for Java installation
New-Item -Path "$env:USERPROFILE\java" -ItemType Directory -Force
```

### 2. Download JDK 21.0.7

```powershell
# Download JDK 21.0.7
$javaUrl = "https://download.oracle.com/java/21/archive/jdk-21.0.7_windows-x64_bin.zip"
$javaOutputPath = "$env:USERPROFILE\Downloads\jdk-21.0.7.zip"
Invoke-WebRequest -Uri $javaUrl -OutFile $javaOutputPath
```

### 3. Extract JDK 21.0.7

```powershell
# Extract JDK to the java directory
Expand-Archive -Path $javaOutputPath -DestinationPath "$env:USERPROFILE\java" -Force
```

### 4. Set up environment variables for Java

```powershell
# Set JAVA_HOME environment variable
[Environment]::SetEnvironmentVariable("JAVA_HOME", "$env:USERPROFILE\java\jdk-21.0.7", "User")

# Update PATH environment variable
$currentPath = [Environment]::GetEnvironmentVariable("PATH", "User")
[Environment]::SetEnvironmentVariable("PATH", "$currentPath;$env:USERPROFILE\java\jdk-21.0.7\bin", "User")
```

### 5. Verify Java installation

```powershell
# Verify Java installation by checking the version
# Note: You may need to restart your PowerShell session for changes to take effect
java -version
```

## Maven Installation

### 1. Create a directory for Maven installation

```powershell
# Create directory for Maven installation
New-Item -Path "$env:USERPROFILE\maven" -ItemType Directory -Force
```

### 2. Download the latest version of Maven

```powershell
# Download the latest Maven (as of May 2025)
$mavenUrl = "https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
$mavenOutputPath = "$env:USERPROFILE\Downloads\maven-latest.zip"
Invoke-WebRequest -Uri $mavenUrl -OutFile $mavenOutputPath
```

### 3. Extract Maven

```powershell
# Extract Maven to the maven directory
Expand-Archive -Path $mavenOutputPath -DestinationPath "$env:USERPROFILE\maven" -Force
```

### 4. Set up environment variables for Maven

```powershell
# Set M2_HOME environment variable
[Environment]::SetEnvironmentVariable("M2_HOME", "$env:USERPROFILE\maven\apache-maven-3.9.6", "User")

# Update PATH environment variable to include Maven
$currentPath = [Environment]::GetEnvironmentVariable("PATH", "User")
[Environment]::SetEnvironmentVariable("PATH", "$currentPath;$env:USERPROFILE\maven\apache-maven-3.9.6\bin", "User")
```

### 5. Verify Maven installation

```powershell
# Verify Maven installation by checking the version
# Note: You may need to restart your PowerShell session for changes to take effect
mvn -version
```

## Alternative: Install using Chocolatey

If you have [Chocolatey](https://chocolatey.org/) installed, you can use the following commands instead:

```powershell
# Install JDK 21
choco install openjdk21 -y

# Install Maven
choco install maven -y
```

## Project Setup

After installing Java and Maven, you can set up the project:

```powershell
# Navigate to your project directory
cd "c:\Users\Admin\Desktop\Projet Fin Etude\PFE"

# Clean and build the project
mvn clean install
```

## Notes

- You may need to restart your PowerShell session after setting environment variables.
- For Oracle JDK downloads, you might need an Oracle account. Consider using OpenJDK alternatives if you encounter download issues.
- Always check for the latest Maven version at [Maven's official website](https://maven.apache.org/download.cgi).
