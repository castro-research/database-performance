# Creating database

```bash
docker run --name=postgres -p 5432:5432 \
    --rm \
    --volume=$(pwd)/schema.sql:/docker-entrypoint-initdb.d/schema.sql \
    --env=POSTGRES_HOST_AUTH_METHOD=trust \
    postgres:17.4 -c log_statement=all
```