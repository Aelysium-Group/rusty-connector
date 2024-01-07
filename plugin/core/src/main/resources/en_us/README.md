# Translation Contribution
Interested in contributing to RC translation?
Be sure to talk with us in [our Discord](http://join.aelysium.group/)!

Make sure that any submitted lang files have been fully tested, and work properly!

## Compiling the zip
The zip should have a name in accordance with RFC4545 naming standards.

When compiling the zip, make sure you zip the parent folder. So in other words:
```yml
en_us.zip:
    en_us:
      - README.md
      - language.yml
        configs:
            mcloader: []
            proxy: []
```
Make sure that both the parent folder and also the zip file have the same name.