# Database write performance

app-1  | Table name test_a size: 651 MB

app-1  | Table name test_b size: 574 MB

## TLDR

The same number of rows, same columns and same and same data in database can have different size. But why?

### PostgreSQL - Data Aligment

When data are alignment ( as the case we can see in test_b table, and createTable on OrderedColumns class), CPU can
perform read and write to memory efficiently. It may help minimizing the amount of padding required while storing a 
tuple on disk, thus saving disk space.

It's exclusive for different data type, but even in different sizes of int, can help saving disk space.

This subject is not too simple to understand, so i will include some references. :)

### References

[PostgreSQL performance tips you have never seen before | Citus Con: An Event for Postgres 2023
](https://www.youtube.com/watch?v=m8ogrogKjXo)

[Data Alignment in PostgreSQL](https://www.enterprisedb.com/postgres-tutorials/data-alignment-postgresql)

[PostgreSQL Column Alignment and Padding – How To Improve Performance With Smarter Table Design](https://www.percona.com/blog/postgresql-column-alignment-and-padding-how-to-improve-performance-with-smarter-table-design/)