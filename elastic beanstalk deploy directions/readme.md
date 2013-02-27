# Elastic Beanstalk Deploy Directions #
Follow this guide to learn how to deploy the `classygamesserver` Eclipse Elastic Beanstalk project to the Amazon Elastic Beanstalk web servers. This guide assumes that you've already got the `classygamesserver` project properly configured and buildable in Eclipse.

1. Make sure that the `classygamesserver` project has no errors. Do a clean and then a full build of the project.

2. Export the project from Eclipse as a WAR file. To do this, select the `classygamesserver` project in the Eclipse Package Explorer, then go to File > Export... > Web > WAR file. Click Next.

3. On this next page of the Export dialog you'll need to make sure of a few things: (A) *Web project* must be set to `classygamesserver`. (B) *Destination* should be set to a valid location, (C) *Target runtime* should have *Optimize for a specific server runtime* checked and *AWS Elastic Beanstalk J2EE Runtime* selected in the dropdown box, (D) *Export source files* should not be checked, and finally, (E) *Overwrite existing file* should be checked. This will place a `.war` file on your desktop.

4. Go to the [Amazon Elastic Beanstalk website](https://console.aws.amazon.com/elasticbeanstalk) in your [AWS Management Console](https://console.aws.amazon.com/console).

5. Click on *Create New Application*.

6. *Application Name:* Classy Games, *Description:* Elastic Beanstalk application for the Classy Games mobile application. *Container Type:* 32bit Amazon Linux running Tomcat 7, for *Application Source* choose *Upload your Existing Application* and then select the `classygamesserver.war` file that you exported earlier. Click *Continue*.

7. Make sure that *Launch a new environment for this application* and *Create an RDS DB Instance with this environment are both checked. *Environment Name* should be `classygames`, *Environment URL* should be `http://classygames.elasticbeanstalk.com`, *Description:* Environment for the Classy Games mobile application. Click *Continue*.

8. *Instance Type:* `t1.micro`, *Application Health Check URL:* `/`. Click *Continue*.

9. The *Create an RDS DB Instance* radio button should be checked, *DB Engine:* `mysql`, *Instance Class:* `db.t1.micro`, *Allocated Storage:* `5GB`, *Deletion Policy:* `Create snapshot`, *Multiple Availability Zones* should be unchecked. Click *Continue*.

10. This page is just a confirmation page. Make sure that everything looks okay and hit *Finish*!