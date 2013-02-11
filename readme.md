Welcome to the Classy Games-Server repository!
======================================
This is the web server side of an Android application. This code was made to be exported as a `war` file and then pushed to [Amazon Elastic Beanstalk](https://aws.amazon.com/elasticbeanstalk/) running 32bit Amazon Linux and Apache Tomcat 7.


Installation Directions
-----------------------
1. Download and install the [Eclipse IDE for Java EE Developers](http://eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/junosr1).
2. Download and install the latest [Java 6 JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html). Note that you really do need Java **6**, not 7. Last I checked this seems to be a limitation with Amazon Elastic Beanstalk. I've built this server code using Java 7, pushed it to Elastic Beanstalk, and seen it not work. Then I just changed the build JDK to 6 and pushed it again and it would then work.
3. Start up Eclipse and check for updates. Restart Eclipse if necessary.
4. Download and install the [AWS Toolkit for Eclipse](https://aws.amazon.com/eclipse/). Again, you'll probably need to restart Eclipse.
5. In Eclipse go to Window > Preferences > Java > Installed JREs.
6. Click *Add...* and then select Standard VM, click *Next*.
7. For *JRE home* I used this: `C:\Program Files\Java\jre6`, and for *JRE name* I used `jre6`. Click *Finish*!
8. You should now see `jre6` right next to `jre7` in your list of Installed JREs. Click OK.
9. Right click on `classygamesserver` in the Package Explorer and choose Properties. Choose Project Facets and make sure that Java is set to 1.6, then click OK.