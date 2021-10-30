# SpringTelegramBot ![Test](https://github.com/UnAfraid/SpringTelegramBot/workflows/Test/badge.svg) [![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/UnAfraid/SpringTelegramBot)

This repository contains an example of telegram bot written in Java (11 and above) using Spring Framework.

This project uses https://github.com/rubenlagus/TelegramBots, check it out for more telegram bot implementation details, also https://core.telegram.org/bots/api for telegram bots API details

Current version supports the following commands:
* /help - Displays help about all or specified command
* /whoami - Displays information about the person who wrote the command: User Id, Name and chat type
* /start - The default bot command, shows greeting message
* /users - Manages users


### Configuration
Configuration is supplied through Environment Variables:

| Environment variable 	    | Required 	| Default value 	| Description                                                                    	                                                                                                                                                    |
|---------------------------|:--------:	|---------------	|--------------------------------------------------------------------------------	                                                                                                                                                    |
| JDBC_URL             	    |    Yes   	|               	| The database url in jdbc format example: jdbc:mariadb://localhost/my_bot       	                                                                                                                                                    |
| JDBC_USERNAME        	    |    Yes   	|               	| The database username example: my_bot                                          	                                                                                                                                                    |
| JDBC_PASSWORD        	    |    Yes   	|               	| The database password                                                          	                                                                                                                                                    |
| TELEGRAM_TOKEN       	    |    Yes   	|               	| The token from [@BotFather](https://t.me/BotFather)                              	                                                                                                                                                    |
| TELEGRAM_USERNAME    	    |    Yes   	|               	| The username from [@BotFather](https://t.me/BotFather)                                                                                                                                                                                |
| TELEGRAM_URL         	    |    Yes   	|               	| The base url on which your bot would listen example: `https://mybot.example.com` 	                                                                                                                                                    |
| TELEGRAM_MAX_CONNECTIONS  |    No   	| 40            	| The Maximum allowed number of simultaneous HTTPS connections to the webhook for update delivery, 1-100. Defaults to 40. Use lower values to limit the load on your bot's server, and higher values to increase your bot's throughput 	|
| TELEGRAM_LANGUAGE_CODE    |    No   	| en            	| A two-letter ISO 639-1 language code. If empty, commands will be applied to all users from the given scope, for whose language there are no dedicated commands                                                                        |
| PORT                 	    |    No    	| 9090          	| The port on which web server will listen for incoming requests                 	                                                                                                                                                    |
