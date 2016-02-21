# TFTI Node.js web service using MongoDB database

*Ignore the most recent comment. This actually does actually work.

This handles user account creation and authentication. Tokens are received through the HTTP header which is then authenticated using Facebook's Graph API. The user is added to the database if not already registered. Once authenticated users are given the an appropriate level of access for their HTTP calls. Users can then create a Spot or join using the generated ID. Location data from the app is then sent to server if the user is at the location and the Spot is updated, ready for other Spot members to retrieve to see who is currently at the Spot's location.
