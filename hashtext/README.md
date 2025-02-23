# Talking about Index

Often, Indexes need a lot of space. The cost of index depends on the column type and how the database system implements it.

In this article, I want to explore how to perform a column text index.

### Before you read

This study document was inspired by Microsoft Developer: Citus Con: An Event for Postgres 2023 (See references)

First:

The usage of hashtext is not documented by PostgreSQL because it's a internal tool. You can find a discussion about [md5 vs hashtext](https://www.postgresql.org/message-id/59df37f0b52657a0f5114684fe96a9cb.squirrel@zenmail.co.za) describing "it is fact roughly 40% faster then md5 AND is an integer"

Second:

The example does not consider hash collisions. Since my example does not use unique constraint, and it is a specific use case, exactly the same as shown on Citus Con. Also, this function was not designed to external usage.

Third:

The result of implementation can change depending on version.

### Start

Imagine you have a long user table, and you noticed a lazy query and you want to improve the performance of this specific use case.

Usually you can create a simple index like:

```bash
INSERT INTO users (email) SELECT 'user' || generate_series(1, 10000000) || '@example.com';
CREATE INDEX ON index_users_email (email);
```

Let's say that we have 10 Millions users, and check the size of email index with psql command: `\di+`

```bash
                                                 List of relations
 Schema |         Name          | Type  |  Owner   |   Table   | Persistence | Access method |  Size  | Description 
--------+-----------------------+-------+----------+-----------+-------------+---------------+--------+-------------
 public | index_users_email     | index | postgres | users     | permanent   | btree         | 680 MB | 
 public | users_pkey            | index | postgres | users     | permanent   | btree         | 214 MB |
```

We can see a [B-TREE Index](https://www.postgresql.org/docs/current/indexes-types.html#INDEXES-TYPES-BTREE), composed of 16kb blocks.

Maybe we can fit the index into memory ?

Consider now, a new table called customers, with exactly the number of rows and data.

```bash
INSERT INTO customers (email) SELECT 'customer' || generate_series(1, 10000000) || '@example.com';
CREATE INDEX ON index_customers_email (hashtext(email));
```

Again, check the size of email index with psql command: `\di+`

```bash
                                                 List of relations
 Schema |         Name          | Type  |  Owner   |   Table   | Persistence | Access method |  Size  | Description 
--------+-----------------------+-------+----------+-----------+-------------+---------------+--------+-------------
 public | customers_pkey        | index | postgres | customers | permanent   | btree         | 214 MB | 
 public | index_customers_email | index | postgres | customers | permanent   | btree         | 277 MB | 
 public | index_users_email     | index | postgres | users     | permanent   | btree         | 680 MB | 
 public | users_pkey            | index | postgres | users     | permanent   | btree         | 214 MB |
```

If your database is really large, this translate to a large index, which might not fit into memory.

But, what did we do to drastically reduce the size from 680mb to 277mb while keeping the same data? 

Basically hashtext of email always returns an integer, insted indexing a large text.

## Drawbacks

- You cannot select the email directly, instead you need to query a select with where email = hashtext(email)

- This is not proper way

### Consideration

Taking into account that you have read the topic 'Before you red', what solution can we use ?

It is always recommended that you follow the official documentation of the chosen database. Evolution happen, and issues get resolved.

If your database is too large and you concerned about index size, maybe using a non-relational database based on the [Vector space model](https://en.wikipedia.org/wiki/Vector_space_model), such as Apache Lucene, OpenSearch, etc... to handle the heaviest workload might be a good approach.


### References

[PostgreSQL performance tips you have never seen before | Citus Con: An Event for Postgres 2023](https://www.youtube.com/watch?v=m8ogrogKjXo)

[PostgreSQL B-Tree Index Explained - PART 1](https://www.qwertee.io/blog/postgresql-b-tree-index-explained-part-1/)

[Which is faster: md5() or hashtext()?](https://www.postgresql.org/message-id/59df37f0b52657a0f5114684fe96a9cb.squirrel@zenmail.co.za)

[https://dba.stackexchange.com/questions/300168/mysql-why-not-create-an-index-on-every-column-what-is-the-cost-of-indexing-a-c](https://dba.stackexchange.com/questions/300168/mysql-why-not-create-an-index-on-every-column-what-is-the-cost-of-indexing-a-c)