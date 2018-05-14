# opencsv-transpose

## Abstracts

In response to a question from [stackoverflow](https://stackoverflow.com/questions/50018101/write-single-bean-rowise-per-attribute-to-csv/50021552#50021552) that transposing from DS1 to DS2, this repository is to show the actually implementations.

DS1:
```html
"name";"age";"sex"
"Jon";"30";"male"
```

DS2:
```html
"name";"Jon";
"age";"30";
"sex";"male";
```

It also supports nested JSON to CSV, which flattens
```json
{
   "name":"Jon",
   "age":"30",
   "sex":"male",
   "mentor":{
      "name":"Mentor Chan",
      "age":"58"
   }
}
```
into
```html
"name";"Jon";
"age";"30";
"sex";"male";
"mentor.name";"Mentor Chan";
"mentor.age";"58";
```

## Usage
```java
java -jar opencsv-transpose <jsonBody>
```
