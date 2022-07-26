create unique index index_lemma_id_page_id_uindex
    on index (lemma_id, page_id);

create unique index index_page_id_lemma_id_uindex
    on index (page_id, lemma_id);

create index page_site_id_index
    on page (site_id);
