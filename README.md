# ChipsList
ChipsList is an example Android application - it's a grocery list program with a lot of initial data, from the USDA. 

* It uses lots of SQLite database features - referential integrity, ORDER BY clause, JOINs.
* Structued with lots of Activities to organize the code. 
* Uses Subclasses the BaseListAdapter to implement SectionIndexer to handle scrolling through a large list of items in real time.
* Walks through the device's SMS database to look for grocery list items sent by other ChipsList users.
* can send an SMS message containing your grocerey list to people in your contact list - if it is split over multiple SMS messages, the recipient has an option to consolidate the lists.  

It works well enough, but has some rough edges - 

TODO: 
* needs to have the database code replaced or augmented with JPA/Hibernate or perhaps ORM is just an anti-pattern and we want to refactor the DB code in a different way.  
* it would be nice to snazz-up the UI with better animations; and use Fragments to re-design the UI.
