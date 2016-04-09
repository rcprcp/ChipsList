# ChipsList
ChipsList is a grocery list program with a lot of initial data, from the USDA. 

* Worked with lots of SQLite database features - referential integrity, ORDER BY clause, JOINs.
* Worked with lots of Activities to organize the code. 
* Subclasses the BaseListAdapter to implement SectionIndexer to handle a large list of items in real time.
* walks through the device's SMS database to look for grocery list items sent by other ChipsList users.

It's a sample Android program, it works well enough, but has some rough edges - 

TODO: 
* needs to have the database code replaced or augmented with JPA/Hibernate.
* it would be nide to snazz-up the UI with better animations.  
* fragment support to look better on a tablet. 
