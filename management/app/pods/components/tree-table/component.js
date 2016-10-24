import Ember from 'ember';

export default Ember.Component.extend({

  afterDomLoad: Ember.on('didInsertElement', function() {
    Ember.$(".product-list.accordion").accordion();
  }),

  numResultsPerPage: Ember.computed('settings.numResultsPerPage', function() {
    return this.get('settings.numResultsPerPage');
  }),
  currentPage: 0,
  maxPage: 0,
  isMultiPage: Ember.computed('settings.data', 'numResultsPerPage', function() {
    const data = this.get('settings.data');
    return data.length && (data.length > this.get("numResultsPerPage"));
  }),
  pageNumbers: Ember.computed('isMultiPage', 'numResultsPerPage', function() {
    if (this.get('isMultiPage')) {
      let ret = [];
      const data = this.get('settings.data');
      let numResults = this.get('numResultsPerPage');
      const numPages = Math.ceil(data.length / numResults);
      this.set('maxPage', numPages);
      for (let i = 1; i <= numPages; ++i) { ret.push(i); }
      return ret;
    }
    return [];
  }),
  currentResultSet: Ember.computed('currentPage', 'numResultsPerPage', 'settings.data', function() {
    let page = this.get('currentPage');
    const results = this.get('settings.data');
    const numResults = this.get('numResultsPerPage');
    const viewable = results.slice(page * numResults, (1 + page) * numResults );
    return viewable;
  }),

  columns: Ember.computed(function() {
    var data = this.get('settings');
    if (!data) {
      return;
    }
    var names = this.get('settings.headers');

    return names;
  }).property('settings.headers'),

  actions: {

    nextPage() {
      const maxPage = this.get("maxPage");
      const curPage = this.get("currentPage");
      if (curPage < maxPage) {
        this.send('goToPage', curPage + 1);
      }
    },
    prevPage() {
      const curPage = this.get("currentPage");
      if (curPage > 0) {
        this.send('goToPage', curPage - 1);
      }
    },
    goToPage(pageNum) {
      const maxPage = this.get("maxPage");
      const curPage = this.get("currentPage");
      if (!pageNum && (curPage === pageNum || pageNum < 0 || pageNum > maxPage)) {
        return;
      }
      this.set('currentPage', pageNum);
    },
    changeNumResults(newResultsPerPage) {
      if (!newResultsPerPage && (newResultsPerPage < 10 || newResultsPerPage> 200)) {
        return;
      }
      // TODO: think about persisting settings.numResultsPerPage on localStorage
      this.set('numResultsPerPage', newResultsPerPage);
    }
  }
});
