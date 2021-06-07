# McLeod Massacre

McLeod Massacre is an open-source Super Smash fangame, with loads of original artwork. This game provides unique
experience to any new or veteran player. In this platform fighter you have the ability to play versus other players on
your machine, or fight against anyone on your local network.

# Table of contents

- [System Requirements](#system-requirements)
- [Installing](#installing)
    - [Installing from GitHub](#installing-from-github)
      - [Via the McLeodMassacre.zip file](#via-the-mcleodmassacrezip-file)
      - [Via unzipped release files](#via-unzipped-release-files)
      - [Building from source](#building-from-source)
- [Licensing](#licensing)
- [Contributing](#contributing)

# System Requirements

||Minimum|Recommended|
|-------------|-------------|-------------|
|OS|Linux (64 bit), MacOS, Windows XP (32 bit), Linux AArch64 | Windows 10 64-bit|
|RAM|1GB|4GB|
|Graphics|OpenGL 2.0|OpenGL 4.6|
|Storage space|200 MB|500 MB|

(Please note that the actual requirements will depend on your OS. The game generally doesn't use more than 700 MB RAM at
any time, but if your OS takes up a lot of the available memory you might need 4GB to actually have enough for the game.
The OpenGL version is just an estimation, you will probably be able to run the game on older systems at the cost of
performance. The storage space used also depends on the file system of your OS, and it keeps changing as we are adding
more and more assets to the game.)

# Installing

Official releases are available on [GitHub](https://github.com/DartProductions/project-MCM/releases), and we are
planning to take this game to other platforms as well.

### Installing from GitHub

#### **Via the McLeodMassacre.zip file**

1. Go to the [releases](https://github.com/DartProductions/project-MCM/releases), and choose the version you want to
   install. We recommend to use the latest stable release.

2. Download the file and extract it to any location on your machine.

3. Run the appropriate launcher file depending on your operating system:

   - For Windows systems (both 32 and 64 bits), use the `run.bat` or `run.exe` files.

   - For 64-bit Linux systems, use the `run-linux64` file.

   - For AArch64 Linux systems, use the `run-linux-aarch64` file.

   - For MacOS systems, use the `run-macos` file.

   You can probably run the game on other operating systems as well, but they are not supported and the game isn't
   guaranteed to work properly on them.

   On Linux and Mac, you migth need to make the launcher file executable via `chmod +x`.

#### **Via unzipped release files**

1. Go to the [releases](https://github.com/DartProductions/project-MCM/releases), and choose the version you want to
   install. We recommend to use the latest stable release.

*Steps 2 and 3 are for Windows users only. Any Linux or MacOS users have to use the provided java files (Step 3/b).*

2. Check your Java version by running the command `java -version` in your console/command line. You can skip this option
   if you are unsure how to do this.

3. If you don't have the latest version (Java 16), please install it. **This game requires Java to run.**

   - Download official releases from [Oracle](https://www.oracle.com/java/technologies/javase-jdk16-downloads.html).

   - Alternatively, you can use the java files provided with the release. Make sure you choose the correct version for
     your operating system (you can download all of the files if you are not sure which one to use). Place the
     downloaded file(s) in your game installation folder.

4. Download the game jar file into your installation folder. It should be named `McLeodMassacre.jar`.

5. You have to use a launcher file specific to your operating system. Download the launcher file into your game
   installation folder.

   - For Windows systems (both 32 and 64 bits), download the `run.bat` or `run.exe` files.

   - For 64-bit Linux systems, download the `run-linux64` file.

   - For AArch64 Linux systems, download the `run-linux-aarch64` file.

   - For MacOS systems, download the `run-macos` file.

   If you are unsure, you can download all of the files and try which one launches the game correctly.

#### **Building from source**

You can use IntelliJ IDEA to import the repository as a project, and use the `-Djava.library.path=target/natives/` JVM
argument to run it from the IDE, or
the `java -Djava.library.path=lib -cp McLeodMassacre.jar dartproductions.mcleodmassacre.Main` command to run the built
jar file.

# Licensing

This game is distributed under [GNU General Public License version 3](https://www.gnu.org/licenses/gpl-3.0.en.html).
This includes all source files, documentation, assets, runtime files, or any other content unless otherwise noted.

There are some files that are _not_ distributed under that license by us.

1. The java files

   This project includes some OpenJDK runtime files for use in its release bundle. They are distributed under their
   original
   license ([GNU General Public License, version 2, with the Classpath Exception](https://openjdk.java.net/legal/gplv2+ce.html))
   . Please check the license for more information.

   The files mentioned:

   - `java.exe` (the `java.exe` file from the Windows/x64 release of OpenJDK 16.0.1 General-Availability Release)

   - `javaw.exe` (the `javaw.exe` file from the Windows/x64 release of OpenJDK 16.0.1 General-Availability Release)

   - `linux-aarch64-java` (the `java` file from the Linux/AArch64 release of OpenJDK 16.0.1 General-Availability
     Release)

   - `linux64-java` (the `java` file from the Linux/x64 release of OpenJDK 16.0.1 General-Availability Release)

   - `osx-java` (the `java` file from the macOs/x64 release of OpenJDK 16.0.1 General-Availability Release)

   The data of these files have not been modified, and we are not claiming any rights for them.

2. Contents of the [lib](https://github.com/DartProductions/project-MCM/tree/engine-dev/src/main/resources/extract/lib)
   folder

   These files belong to other projects we used during the devolopment of this game. Please check the license
   of [JInput](https://jinput.github.io/jinput/) for the license used for these files.

   We are distributing these files under their original license. They are unmodified and we are not claiming and rights
   to them.

# Contributing

As a free and open-spource game, McLeod Massacre was created by the fan community, and we are hoping to keep it that
way. You are allowed to create your own extensions that can make it into this game after they complete the review
process.

