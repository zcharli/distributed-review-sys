import Ember from 'ember';

export default Ember.Component.extend({
  numResultsPerPage: Ember.computed('settings.numResultsPerPage', function() {
    return this.get('settings.numResultsPerPage');
  }),
  currentPage: 1,
  maxPage: 0,
  isMultiPage: Ember.computed('product.childLevel', 'numResultsPerPage', function() {
    const data = this.get('product.childLevel');
    return data.length && (data.length > this.get("numResultsPerPage"));
  }),
  pageNumbers: Ember.computed('isMultiPage', 'numResultsPerPage', function() {
    if (this.get('isMultiPage')) {
      let ret = [];
      const data = this.get('product.childLevel');
      let numResults = this.get('numResultsPerPage');
      const numPages = Math.ceil(data.length / numResults);
      this.set('maxPage', numPages);
      for (let i = 1; i <= numPages; ++i) { ret.push(i); }
      return ret;
    }
    return false;
  }),
  currentResultSet: Ember.computed('currentPage', 'numResultsPerPage', 'product.childLevel', function() {
    let page = this.get('currentPage') - 1;
    const results = this.get('product.childLevel');
    const numResults = this.get('numResultsPerPage');
    return results.slice(page * numResults, (1 + page) * numResults );
  }),
  numColumns: Ember.computed('columns', function() {
    const cols = this.get('columns');
    return cols && cols.length;
  }),

  rows: Ember.computed('currentResultSet', 'settings.columnOrder', function() {
    const order = this.get('settings.columnOrder');
    const results = this.get('currentResultSet');
    let ret = [];
    for (let i = 0; i < results.length; ++i) {
      const result = results[i];
      let thisRow = [];
      for (let i = 0; i < order.length; ++i) {
        thisRow.push(result.get(order[i]));
      }
      ret.push({
        id: result.get('contentId') + result.get('locationId'),
        data: thisRow
      });
    }
   return ret;
  }),
  productLabel: Ember.computed('product', function() {
    if (!this.get('product.parentLevel.identifier')) {
      return false;
    }
    return `${this.get('product.parentLevel.identifier')} ${this.get("product.parentLevel.name")}`;
  }),

  actions: {
    openEditModal(id) {
      console.log(id);
      Ember.$(".ui.basic.modal."+id).modal('show');
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
