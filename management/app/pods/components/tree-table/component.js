import Ember from 'ember';

export default Ember.Component.extend({
  dimmerContainer: null,
  hasLoaded: false,

  afterDomLoad: Ember.on('didInsertElement', function() {
    Ember.$(".product-list.accordion").accordion();
  }),

  numResultsPerPage: Ember.computed('perpage', function() {
    return this.get('perpage');
  }),
  currentPage: 1,
  maxPage: 0,
  isMultiPage: Ember.computed('settings.data', 'numResultsPerPage', function() {
    console.log("change detected multipage");
    const maxNum = this.get("numResultsPerPage") || 10;
    const data = this.get('settings.data');
    return data.length && (data.length > maxNum);
  }),
  pageNumbers: Ember.computed('isMultiPage', 'numResultsPerPage', function() {
    if (this.get('isMultiPage')) {
      let ret = [];
      const data = this.get('settings.data');
      let numResults = this.get("numResultsPerPage") || 10;
      const numPages = Math.ceil(data.length / numResults);
      this.set('maxPage', numPages);
      for (let i = 1; i <= numPages; ++i) { ret.push(i); }
      return ret;
    }
    return [];
  }),
  currentResultSet: Ember.computed('currentPage', 'numResultsPerPage', 'settings.data', function() {
    console.log("change detected");
    if (this.get("hasLoaded")) {
      this.send("dimAndShowLoader");
    } else {
      this.set("hasLoaded", true);
    }
    let page = this.get('currentPage') - 1;
    const results = this.get('settings.data');
    const numResults = this.get("numResultsPerPage") || 10;
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

  onDomLoad: Ember.on("didInsertElement", function() {
    console.log('didInsertElement');
    const dimmerContainer = Ember.$(".tree-table-list-load-dimmer");
    this.set("dimmerContainer", dimmerContainer);
  }),

  updatedDom: Ember.on("didUpdate", function() {
    console.log('didUpdate');
    this.send("removeDimAndLoader");
  }),

  actions: {
    passAction(param) {
      const parentAction = this.get('action');
      if (parentAction) {
        this.sendAction("action", param);
      }
    },

    dimAndShowLoader() {
      const dimmer = this.get("dimmerContainer");
      dimmer.addClass("active");
      console.log(dimmer);
    },

    removeDimAndLoader() {
      const dimmer = this.get("dimmerContainer");
      dimmer.removeClass("active");
      console.log(dimmer);
    },

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
      console.log(maxPage, curPage);
      console.log(pageNum);
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
