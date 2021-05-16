# Welcome to the QTD-Bot!
This is the repository for the Quit to Desktop Discord-Server Bot.

Feel free to implement features or fix things. You can create Pull requests for your changes.

## Auto Deployment (CD)
The repository is currently set up with an auto deployment feature.
After every push to the `main`-branch the `target\-shaded.jar` and `start.sh` get uploaded to the server.
Then the `start.sh` gets executed (= kill all screen and bot processes, start bot).

**Note:** There is no CI set up. So you have to manually build the bot on your local machine: `mvn clean install` and `mvn package`.
