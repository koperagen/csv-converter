A toolbox for semi-automated localization

1. Convert properties file (part i18n bundle) to CSV

    CSV seems to be natural fit for representing some entity with several parameters - localization variants in our case.
    Yet, string resources usually stored as `key=value` files.
    Basic conversion leaves us with desired columns - localization variants, and bunch of empty cells.
   
```
   |  COL_ID |  en_EN  |   ....  |  ru_RU  |
   |---------|---------|---------|---------|
   |    id   |  var1   |   ....  |         |
   |   ....  |  ....   |   ....  |   ....  |
   |   idN   |  var1   |   ....  |         |
   -----------------------------------------
```

2. Translate texts using edited CSV as cache
    
    At this point you may want to preprocess data, manually edit sensitive formatting etc.
    You can do that by editing CSV from step 1. Later, this process could be repeated after machine translation till perfection.
    Algorithms will treat your time and API quotas with respect, as follows: 
    
    Let's say, we have following CSV.

```
 |  COL_ID |  en_EN  |   ....  |  ru_RU  |
 |---------|---------|---------|---------|
 |    id   |  var1   |   ....  |   var3  |
 |   ....  |  ....   |   ....  |   ....  |
 |   idN-1 |  var1   |   ....  |    *    |
 |   idN   |  var1   |   ....  |         |
 -----------------------------------------
```

Text with ID `idN` isn't yet translated, so ru_RU cell can be updated with machine translation. If cell isn't empty, its content will be left unchanged. To skip translation of particular text, place `*` in cell

3. **WIP** Download CSV with texts from Google sheets
    
    

4. Convert CSV to multiple properties files

    Finally, convert CSV's back to project files