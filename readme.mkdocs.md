# Installation of mkdocs

To build docs you need mkdocs and mkdocs-material theme installed:
<https://www.mkdocs.org/#installing-mkdocs>

    pip install mkdocs
    pip install mkdocs-material

# Building docs

This will create `site` directory:
<https://www.mkdocs.org/#building-the-site>

    cd to/parent/of/docs/folder
    mkdocs build

# Localhost web-server starting

This will create local web-site on 127.0.0.1:8000:
<https://www.mkdocs.org/#getting-started>

    mkdocs serve

# Deploying docs

This will create `gh-pages` branch in repository:
<https://www.mkdocs.org/user-guide/deploying-your-docs/#project-pages>

    git checkout master
    mkdocs gh-deploy 