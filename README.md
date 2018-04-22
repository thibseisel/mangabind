*MangaBind* is a software that allows you to download manga scans (also known as "scantrads") from the internet.
This makes it easier to pack those images into a file that can be read on your e-Reader.

(Here, I should put a photo of a e-Reader with a manga displayed on it)

- **Fast** : it can download entire chapters to your computer in a few seconds\*.
- **Robust** : when a download fails, it retries with different URLs until the download succeeds or every attempt have failed. 
MangaBind is smart enough to detect missing pages, double-pages or the legitimate end of a chapter.
- **Configurable** : you are in control from where you'd like to download chapters, MangaBind does the rest.

\* Of course, this also depends on the source website and the quality of your bandwidth.

## Configuring manga sources ##

Before executing the program, you have to write a configuration file describing what mangas are available for download 
and from which websites.

```
At the moment, no default configuration file is provided because we don't want to be opinionated 
about what mangas worth being read first! 
We might add one with the most popular mangas at a later time.
```

Using your favorite file editor (not Microsoft Word, though), create a file `mangasource.json` and edit it.
Here is a sample structure that you can copy-paste and replace with your own values:

```json
[
  {
    "id": 1,
    "title": "Your favorite manga's name",
	"start_page": 1,
    "single_pages": [
		"http://a.website.com/path/to/manga/[c]/[2p].jpeg",
		"http://a.website.com/math/to/manga/[c]/[2p].png"
	],
    "double_pages": []
  }
]
```

Each manga is defined between curly braces `{ ... }`.
Here is a description of each configurable properties:

- `id` : a number unique to each manga. You can pick any number, but two mangas cannot share the same.
- `title` : the title of the manga it refers to.
- `start_page` : the index of the first page chapters starts from. 
While most websites starts at 1, others may start at 0, or you may want to always skip the `n` first pages. 
- `single_pages` : a comma-separated list of template urls pointing to images to download.
Those templates should contain special sequences of characters between square brackets `[...]` 
to be replaced with the chapter number `[c]` and the page number `[p]`.
Specifying multiple URLs makes MangaBind more robust
- `double_pages` : sometimes, websites give special names to scantrads that spanws on two pages, 
such as `manga-12-13.png`. 
Therefore, this property is the same as `single_pages` but with an additional special `[q]` to be replaced with 
the number of the second page.

After downloading the software, put the `mangasource.json` file into the `bin` folder.

## Downloading ##

You just have to execute the start script `manbagind` under the `bin` folder. 
Windows users can double-click on `mangabind.bat`.

Then, you have to select a manga from the displayed table and give a range of chapters to be downloaded.
For example, if you want to download scantrads from the 3rd volume of Naruto, 
type the number displayed next to Naruto in the displayed table and press enter.

You are asked for the range of chapters to download, type `18-27` to download chapters 18 to 27 
(that are part of the 3rd volume), and press enter.

MangaBind informs you of the current download progress, telling you when pages are missing.
Be attentive to the number of pages it has downloaded! If there are too few, this may be sign 
that your configuration is incomplete and should feature more URLs.

When finished, you can find he downloaded pages into the `pages` folder.

## Roadmap ##

While perfectly working, MangaBind still lacks some serious features such as
* a way to pack downloaded images into a CBZ (Comic Book Zip Archive) file, 
* a built-in manga sources configuration file to speed up setting up,
* a more user-friendly way to edit configuration files than JSON.
