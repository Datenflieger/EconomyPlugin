# CoinSystem

A comprehensive economy plugin for Minecraft servers that provides a complete coin management system.

## Features

- **Player Balance Management**: View, set, give, and take coins from players
- **Pay System**: Allow players to transfer coins to each other
- **Top Balances**: View the richest players on the server with a GUI interface
- **Death Penalty**: Players lose 20% of their coins when they die
- **Starting Balance**: New players automatically receive 1000 coins
- **PlaceholderAPI Support**: Use placeholders to display coin information
- **MySQL Support**: Store player data in a MySQL database

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/money` or `/coins` or `/balance` | View your own balance | None |
| `/money <player>` | View another player's balance | `coinsystem.view.others` |
| `/money set <amount>` | Set your own balance | None |
| `/money set <player> <amount>` | Set another player's balance | `coinsystem.set` |
| `/money give <amount>` | Give yourself coins | None |
| `/money give <player> <amount>` | Give coins to another player | `coinsystem.give` |
| `/money take <amount>` | Take coins from yourself | None |
| `/money take <player> <amount>` | Take coins from another player | `coinsystem.take` |
| `/pay <player> <amount>` | Send coins to another player | None |
| `/baltop` or `/moneytop` | View the richest players on the server | None |

## Placeholders

The plugin provides the following placeholders for use with PlaceholderAPI:

| Placeholder | Description |
|-------------|-------------|
| `%coinsystem_player_coins%` | Displays the player's coin balance |
| `%coinsystem_server_wirtschaft%` | Displays the total economy of the server |

## Installation

1. Download the latest version of the plugin
2. Place the JAR file in your server's `plugins` folder
3. Start or restart your server
4. Configure the plugin in the `config.yml` file

## Configuration

### config.yml

```yaml
database:
  type: mysql # or mysql
  host: localhost
  port: 3306
  name: minecraft
  user: root
  password: password
```

### messages.yml

You can customize all messages in the `messages.yml` file.

## Dependencies

- [Vault](https://www.spigotmc.org/resources/vault.34315/)
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)

## Support

If you encounter any issues or have questions, please create an issue on the GitHub repository.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Author

Created by Datenflieger 