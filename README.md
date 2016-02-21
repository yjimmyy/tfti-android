# TFTI Android app

*Ignore the most recent comment. This actually does actually work.

Users can log in using Facebook which generates the access token. Data is sent and received from web server via http calls using the token for authentication. Users can create a Spot, which is like a group for a designated location, by setting a location and its radius which other users can then join. Users may join a pre-existing group by using a Spot's ID. The app periodically polls the device's location data and updates the server ONLY IF the user is inside a Spot (no creepy location logging to sell your data to the Man). Data retrieved from server is stored locally using SQLite.

Now you can see who's at the rock gym without having to spam message everyone only to get a response a hour later telling you they're about to leave soon.
