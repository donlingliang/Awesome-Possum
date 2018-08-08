# Awesome-Possum

Room:
1. To use Android Room for prepopulated database: https://issuetracker.google.com/issues/62185732#comment13
- Using https://github.com/daolq3012/AssetSQLiteOpenHelper can solve it together with putting the database in the `assets/databases`
- Instead of saving database into `assets`, we can write `FrameworkSQLiteDatabase` and Our own `SQLiteOpenHelper`
