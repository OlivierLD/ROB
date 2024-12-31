# PHP Tide Computer

Work in progress, indeed.  
Here, size does matter.  
`php` array representation of the stations is too big (fails in memory allocation).  
Same when reading the corresponding JSON file.  

Trying a RDBMS approach with `sqlite`... That seems to work. This requires the DB to exist and to be available (`sql/tides.db`).  
And the `SQLite` drivers are available in `php`...  

More soon.

See:
- <https://www.php.net/manual/en/sqlite3.query.php>
- <https://www.php.net/manual/en/book.sqlite3.php>


## A test
From the `php` folder, start the PHP server:  
```
$ php -S localhost:8000
```
Then from a browser, reach `http://localhost:8000/tide.sample.php`.  
Or from a terminal, do a 
```
$ curl http://localhost:8000/tide.sample.php
```
or
```
$ curl http://localhost:8000/tide.workbench.php
```


---
