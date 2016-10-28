package metric;

/**
 * Created by cli on 10/28/2016.
 */

// Average review per product
// Total number of products tracked
// Total number of reviews tracked
// Average reviews per product
// Average length of review
// Average product stars

// Reviews submitted by last week 9 (implement)

// Top 10 most viewed products
// Top 10 most upvoted products
// Top 10 newest approved reviews

// Percent of product types
// Average number of stars per product type
// Amount of disk space used

public enum MetricType {
    AVG_REVIEW_PER_PRODUCT {
        public String getComponent() {
            return "metric-single-stat";
        }
        public String getName() {
            return "Average Review per Product";
        }
        public String getDescription() {
            return "Since begining of time.";
        }
        public String getPagePosition() {
            return "top";
        }
        public String getIcon() {
            return "";
        }
    },
    TOTAL_PRODUCTS_TRACKED {
        public String getComponent() {
            return "metric-single-stat";
        }
        public String getName() {
            return "Total Products Tracked";
        }
        public String getDescription() {
            return "Since begining of time.";
        }
        public String getPagePosition() {
            return "top";
        }
        public String getIcon() {
            return "";
        }
    },
    TOTAL_REVIEWS_TRACKED {
        public String getComponent() {
            return "metric-single-stat";
        }
        public String getName() {
            return "Total Reviews Tracked";
        }
        public String getDescription() {
            return "Since begining of time.";
        }
        public String getPagePosition() {
            return "top";
        }
        public String getIcon() {
            return "";
        }
    },
    AVG_REVIEWS_PER_PRODUCT {
        public String getComponent() {
            return "metric-single-stat";
        }
        public String getName() {
            return "Average Reviews Per Product";
        }
        public String getDescription() {
            return "Since begining of time.";
        }
        public String getPagePosition() {
            return "top";
        }
        public String getIcon() {
            return "";
        }
    },
    AVG_REVIEW_LENGTH {
        public String getComponent() {
            return "metric-single-stat";
        }
        public String getName() {
            return "Average Review Length";
        }
        public String getDescription() {
            return "Since begining of time.";
        }
        public String getPagePosition() {
            return "top";
        }
        public String getIcon() {
            return "";
        }
    },
    AVG_PRODUCT_STARS {
        public String getComponent() {
            return "metric-single-stat";
        }
        public String getName() {
            return "Average Product Stars";
        }
        public String getDescription() {
            return "Since begining of time.";
        }
        public String getPagePosition() {
            return "top";
        }
        public String getIcon() {
            return "";
        }
    },
    REVIEWS_SUBMITTED_LAST_7 {
        public String getComponent() {
            return "metric-bar-chart";
        }
        public String getName() {
            return "Reviews Submitted (Last 7 Days)";
        }
        public String getDescription() {
            return "An aggregate of published reviews in the last 7 days";
        }
        public String getPagePosition() {
            return "middle";
        }
        public String getIcon() {
            return "";
        }
    },
    TOP_10_VIEWED_PRODUCT {
        public String getComponent() {
            return "metric-list";
        }
        public String getName() {
            return "Top 10 Viewed Products";
        }
        public String getDescription() {
            return "Across all product categories";
        }
        public String getPagePosition() {
            return "middle";
        }
        public String getIcon() {
            return "";
        }
    },
    TOP_10_UPVOTED_PRODUCT {
        public String getComponent() {
            return "metric-list";
        }
        public String getName() {
            return "Top 10 Upvoted Products";
        }
        public String getDescription() {
            return "Across all product categories";
        }
        public String getPagePosition() {
            return "";
        }
        public String getIcon() {
            return "";
        }
    },
    TOP_10_NEWEST_REVIEWS {
        public String getComponent() {
            return "metric-list";
        }
        public String getName() {
            return "Top 10 Newest Reviews";
        }
        public String getDescription() {
            return "Across all product categories";
        }
        public String getPagePosition() {
            return "";
        }
        public String getIcon() {
            return "";
        }
    },
    PERCENT_PRODUCT_TYPE {
        public String getComponent() {
            return "metric-pie-chart";
        }
        public String getName() {
            return "Product by Type";
        }
        public String getDescription() {
            return "Across all tracked products";
        }
        public String getPagePosition() {
            return "";
        }
        public String getIcon() {
            return "";
        }
    },
    AVG_STARS_PER_PRODUCT_TYPE {
        public String getComponent() {
            return "metric-pie-chart";
        }
        public String getName() {
            return "Average Stars Per Product Type";
        }
        public String getDescription() {
            return "";
        }
        public String getPagePosition() {
            return "";
        }
        public String getIcon() {
            return "";
        }
    },
    AMOUNT_DISK_SPACE {
        public String getComponent() {
            return "metric-pie-chart";
        }
        public String getName() {
            return "Disk Space Left";
        }
        public String getDescription() {
            return "";
        }
        public String getPagePosition() {
            return "";
        }
        public String getIcon() {
            return "";
        }
    };

    abstract String getComponent();
    abstract String getIcon();
    abstract String getName();
    abstract String getDescription();
    abstract String getPagePosition();
}
