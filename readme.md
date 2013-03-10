Welcome to the Classy Games-Server repository!
======================================
This is the web server side of an Android application. This code was made to be exported as a `war` file and then pushed to [Amazon Elastic Beanstalk](https://aws.amazon.com/elasticbeanstalk/) running 32bit Amazon Linux and Apache Tomcat 7.


Installation Directions
-----------------------
1. Download and install the [Eclipse IDE for Java EE Developers](http://eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/junosr1).
2. Download and install the latest [Java 6 JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html). Note that you really do need Java **6**, not 7. Last I checked this seems to be a limitation with Amazon Elastic Beanstalk. I've built this server code using Java 7, pushed it to Elastic Beanstalk, and seen it not work. Then I just changed the build JDK to 6 and pushed it again and it would then work.
3. Start up Eclipse and go to *Help* > *Check for Updates*. Restart Eclipse if necessary.
4. Download and install the [AWS Toolkit for Eclipse](https://aws.amazon.com/eclipse/). Again, you'll probably need to restart Eclipse. And do yourself a favor and check for updates after this restart too.
5. In Eclipse go to *Window* > *Preferences* > *Java* > *Installed JREs*.
6. Click *Add...* and then select *Standard VM*, click *Next*.
7. For *JRE home* I used this: `C:\Program Files\Java\jre6`, and for *JRE name* I used `jre6`. Click *Finish*!
8. You should now see `jre6` right next to `jre7` in your list of Installed JREs. Click OK.
9. Import the `importableclassygamesserver.war` file located in this directory into Eclipse by doing *File* > *Import* > *Web* > *WAR File* > *Next*.
10. Now for *WAR file:* click *Browse* and find the `importableclassygamesserver.war` file that you just downloaded, for *Web project:* rename it to `classygamesserver`, for *Target runtime:* make sure that it's *AWS Elastic Beanstalk J2EE Runtime*, and then for *EAR membership* make sure that the box is unchecked. Click *Next*.
11. You should see a screen asking about importing Web libraries and shown a list of `.jar` files. Make sure that all of the boxes are unchecked and click *Finish*.
12. Right click on `classygamesserver` in the Package Explorer and choose *Properties*. Choose *Project Facets* and make sure that *Java* is set to *1.6*, then click *OK*.
13. Now you've imported the project but are going to be missing all of the source files. You're going to need to download this repository as it currently is and paste in those source files.

Changelog
---------
Classy Games Server v1.5.1 was released on March 9th, 2013, and includes support for the new `ForfeitGame` function.

API Documentation
-----------------
This is an attempt to document exactly how to make API calls against the Classy Games server code. All of the below endpoints **require** HTTP POST requests in order for them to gather data, **not HTTP GET**! All of these endpoints will respond to your input with a `UTF-8` encoded JSON String (`application/json; charset=UTF-8`) as output.

+ ### GetGame `http://classygames.net/GetGame` ###
    + Outputs the state of the game board as a `JSON` String. Is used to acquire the board data for a specific game.

    + *Input*
        + **`id`** a String that closely resembles a `SHA-256` hash.
        + **Example input**
            + `string`

    + *Output*
        + Hello, World!
        + **Example output**
            + `string`

+ ### GetGames `http://classygames.net/GetGames` ###
    + Outputs a list of games that the given user has. This output will only contain in progress games.

    + *Input*
        + **`id`** a String, that represents the current user's Facebook id. As a numeral this would be a `long`.
        + **Example input**
            + `string`

    + *Output*
        + Hello, World!
        + **Example output**
            + `string`

+ ### ForfeitGame `http://classygames.net/ForfeitGame` ###
    + Allows a user to quit (and therefore lose) a game that has not already been finished.

    + *Input*
        + **`id`** a String
        + **Example input**
            + `string`

    + *Output*
        + Hello, World!
        + **Example output**
            + `string`

+ ### NewGame `http://classygames.net/NewGame` ###
    + Creates a new game of the specified game type with the specified user.

    + *Input*
        + Hello, World!
        + **Example input**
            + `string`

    + *Output*
        + Hello, World!
        + **Example output**
            + `string`

+ ### NewMove `http://classygames.net/NewMove` ###
    + Creates a new move in an already existing game.

    + *Input*
        + Hello, World!
        + **Example input**
            + `string`

    + *Output*
        + Hello, World!
        + **Example output**
            + `string`

+ ### NewRegId `http://classygames.net/NewRegId` ###
    + Registers a user with the server. This is needed primarily for push (GCM - Google Cloud Messaging) notifications.

    + *Input*
        + Hello, World!
        + **Example input**
            + `string`

    + *Output*
        + Hello, World!
        + **Example output**
            + `string`

+ ### RemoveRegId `http://classygames.net/RemoveRegId` ###
    + Removes a user's device ID from the server. The user will no longer be able to receive push (GCM - Google Cloud Messaging) notifications until they register again. This does not remove a user's data from the database entirely however.

    + *Input*
        + Hello, World!
        + **Example input**
            + `string`

    + *Output*
        + Hello, World!
        + **Example output**
            + `string`

+ ### SkipMove `http://classygames.net/SkipMove` ###
    + Allows someone to pass. The given game will then switch to the other user's turn.

    + *Input*
        + Hello, World!
        + **Example input**
            + `string`

    + *Output*
        + Hello, World!
        + **Example output**
            + `string`