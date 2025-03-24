# Database Schema

## Tables

### `users`

| Column Name | Data Type   | Details               |
|-------------|-------------|-----------------------|
| id          | long        | not null, primary key |
| first_name  | varchar(50) | not null              |
| last_name   | varchar(50) | nullable              |
| username    | varchar(50) | not null              |

### `locations`

| Column Name | Data Type   | Details               |
|-------------|-------------|-----------------------|
| id          | long        | not null, primary key |
| city        | varchar(50) | not null              |
| country     | varchar(50) | not null              |
| latitude    | decimal     | not null              |
| longitude   | decimal     | not null              |

### `user_locations`

| Column Name | Data Type | Details               |
|-------------|-----------|-----------------------|
| id          | long      | not null, primary key |
| user_id     | long      | not null, foreign key |
| location_id | long      | not null, foreign key |