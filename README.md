# Tater Bot
[![Travis CI Build Status](https://travis-ci.org/bradhandy/tater-bot.svg?branch=master)](https://www.travis-ci.org/bradhandy/tater-bot)
[![Java 8+](https://img.shields.io/badge/java-8+-blue.svg?maxAge=3600)](http://java.oracle.com/)
[![License Apache 2.0](https://img.shields.io/badge/license-Apache%202.0-orange.svg?maxAge=3600)](https://raw.githubusercontent.com/bradhandy/tater-bot/master/LICENSE)

Tater Bot is a bot connected to the Line (https://line.me/) chat servers.  Line is the chat platform of
choice for players in the top-level guilds of Dungeon Boss.

### Planned Features
This chat bot will provide functionality to enhance your Line experience.  The following is a list of the
functionality provided:

* Chat Recording:  Start a recorded discussion.  This allows the discussion to be stored as record of
topics discussed and decisions made for those topics without having to export the data.
* Snarky Responses:  If ask Tater Bot what it can do, it will respond with something snarky while also
displaying the help command output.
* Trivia:  Host a trivia session in your group chat.  Submit a set of questions, start the session, and
watch the fun ensue.  Questions can be multiple choice or free form, and can be submitted via private
message or within a channel.  Questions submitted as a trivia session become a part of the larger
question database for your channel after the session has completed.

  You can also have Tater Bot ask you a question outside of a trivia session.  Tater Bot will keep track
  of your overall progress so you can brag about being the most knowledgeable.
  
* Service Enable/Disable:  The ability to enable/disable any of the services the bot offers.  Snarky commentary
  is a service which allows the bot to produce snarky output for certain commands.  If you don't want the snarky
  commentary, then disable it.  Other services can be enabled/disabled as well.
  
  
### Dependencies
The bot uses the following software dependencies:

* [[Line Bot SDK for Java](https://github.com/line/line-bot-sdk-java)] (https://github.com/line/line-bot-sdk-java) 
* [[Spring Boot](https://spring.io/projects/spring-boot)] (https://spring.io/projects/spring-boot)
* [[ANTLR](http://www.antlr.org/)] (http://www.antlr.org/)
* [[SLF4J](https://www.slf4j.org/)] (https://www.slf4j.org/)


* Test Dependencies
  * [[JUNIT 5](http://www.junit.org/)] (http://www.junit.org/)
  * [[Mockito](https://site.mockito.org/)] (https://site.mockito.org/)