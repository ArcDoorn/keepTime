keepTime
========
  A personal time logging application to keep track of working hours.

Intro
-----
   This project has two proposes: First, I need some software to 
   keep track about my working hours. Second, I want to learn more 
   Clojure (and ClojureScript).

   At the moment the project is an pretty early phase: It can be used 
   to log time (manually), but many features, like calculating the worked
   hours, are still missing. I hope to get them working soon.

   You can look in the backlog.org to get an overview of possible
   features and the order, in which I plan to work on them.

Usage
-----
   In the current state of the project you need an installation of 
   Leiningen (version 2) to compile the project. With "lein uberjar"
   you can build a single jar file. 

   It is expected, that the database file (timeData.sqlite) is in the 
   directory from which you start keepTime.

   Once started, you can open http://localhost:8080/day/2012-12-24
   and start logging time.

   In few command lines:

   $ git clone https://github.com/ArcDoorn/keepTime.git

   $ cd keepTime

   $ lein run 

User Interface
--------------

The user interface is given as a web page (using Noire and
ClojureScript). The plan is to go over to a single page application.
You can click on the items and a box will open for editing. Klicking
on okay (at the moment only an o is shown), the box is closed, the
changes are saved in the data base and the page is reloaded.

For the design I was inspired by the Maya, whose calendar cycle
stared again at 2012-12-21, close to the date, when I started 
the project.

License
-------
   Copyright (C) 2012 Sebastian Panknin <doorn@web.de>
   Distributed under the Eclipse Public License, the same as Clojure.


