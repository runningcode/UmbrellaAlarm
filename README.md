UmbrellaAlarm
=============

Warns you when it is going to rain so you can bring an umbrella!

The main activity of the app is a PreferenceActivity to change settings. The rest of the app runs in the background.

This app has two services.
The CheckAlarmService runs every hour for those who want the notification to happen when their alarm goes off. It schedules the UmbrellaAlarmService for the time of the user's next alarm.
The UmbrellaAlarmService is scheduled to run either based on the user's alarm or at a time the user picked in the settings.
