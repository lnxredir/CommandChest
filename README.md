# CommandChest

A Minecraft plugin for Paper/Spigot that transforms chests into command-executing blocks. Configure chests to run commands when players interact with them, with support for cooldowns, required items, multiple activation methods, and hologram displays.

## Features

- **Command Execution**: Configure any chest to execute commands when activated
- **Multiple Activation Methods**: Choose between left click, right click, both clicks, or shift click activation
- **Cooldown System**: Set per-chest cooldowns to prevent spam
- **Item Requirements**: Require players to hold specific items to activate chests
- **Custom Names**: Set custom multi-line names for chests with visibility toggle
- **Hologram Support**: Optional integration with FancyHolograms to display chest names above blocks
- **GUI Configuration**: Easy-to-use graphical interface for configuring chests
- **Per-Player Cooldowns**: Cooldowns are tracked per player, not globally
- **Persistent Storage**: All chest configurations are saved and loaded automatically

## Requirements

- Minecraft 1.21
- Paper or Spigot server
- FancyHolograms (optional, for hologram support)

## Installation

1. Download the latest release from the releases page
2. Place the `CommandChest-1.0.0-build*.jar` file into your server's `plugins` folder
3. Restart your server
4. The plugin will generate a `config.yml` file in the `plugins/CommandChest/` directory

## Usage

### Getting Started

1. Run `/cchest` to receive the Configuration Stick
2. Hold the Configuration Stick and right-click a chest (or other container block) to open the configuration GUI
3. Configure the chest settings through the GUI
4. Save your configuration

### Configuration GUI

The configuration GUI allows you to set:

- **Chest Name**: Multi-line custom name displayed above the chest (supports color codes)
- **Command**: The command to execute when the chest is activated (without the leading `/`)
- **Cooldown**: Time in seconds between activations per player
- **Activation Method**: How the chest should be activated (left click, right click, both, or shift click)
- **Required Item**: An item that must be held to activate the chest
- **Visibility**: Toggle whether the chest name is visible

### Activation Methods

- **Left Click**: Chest activates only on left click
- **Right Click**: Chest activates only on right click
- **Both**: Chest activates on either left or right click
- **Shift**: Chest activates only when shift-clicking

### Example Commands

When setting a command in the GUI, enter it without the leading slash. For example:

- `spawn` - Teleports player to spawn
- `give @p diamond 1` - Gives the player a diamond
- `effect @p minecraft:speed 30 1` - Applies speed effect
- `say Hello from CommandChest!` - Broadcasts a message

## Configuration

All messages and UI text can be customized in `plugins/CommandChest/config.yml`. This file supports full color code customization and is useful for translations or server-specific customization.

### Key Configuration Sections

- `messages.command`: Command-related messages
- `messages.gui`: GUI button labels and descriptions
- `messages.chat`: Chat input prompts
- `messages.activation`: Activation feedback messages
- `messages.config`: Configuration status messages

## Permissions

- `commandchest.use`: Allows use of the `/cchest` command (default: op)
- `commandchest.admin`: Allows configuration of command chests (default: op)
