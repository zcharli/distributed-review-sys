package metric;

import config.APIConfig;

/**
 * Created by cli on 10/28/2016.
 */

// Average review per product
// Total number of products tracked
// Total number of reviews tracked
// Acceptance Rate
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
            return "comment icon";
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
            return "shopping basket icon";
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
            return "list icon";
        }
    },
    ACCEPTANCE_RATE {
        public String getComponent() {
            return "metric-single-stat";
        }
        public String getName() {
            return "Review Acceptance Rate";
        }
        public String getDescription() {
            return "Since begining of time.";
        }
        public String getPagePosition() {
            return "top";
        }
        public String getIcon() {
            return "checkmark box icon";
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
            return "In terms of words";
        }
        public String getPagePosition() {
            return "top";
        }
        public String getIcon() {
            return "write icon";
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
            return "Per average stars on reviews";
        }
        public String getPagePosition() {
            return "top";
        }
        public String getIcon() {
            return "star icon";
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
            return "history icon";
        }
    },
    TOP_10_VIEWED_PRODUCT {
        public String getComponent() {
            return "metric-list";
        }
        public String getName() {
            return "Top " + APIConfig.TOP_X_METRIC + " Viewed Products";
        }
        public String getDescription() {
            return "Across all product categories";
        }
        public String getPagePosition() {
            return "middle";
        }
        public String getIcon() {
            return "ordered list icon";
        }
    },
    TOP_10_UPVOTED_PRODUCT {
        public String getComponent() {
            return "metric-list";
        }
        public String getName() {
            return "Top " + APIConfig.TOP_X_METRIC + " Upvoted Products";
        }
        public String getDescription() {
            return "Across all product categories";
        }
        public String getPagePosition() {
            return "middle";
        }
        public String getIcon() {
            return "thumbs up icon";
        }
    },
    TOP_10_NEWEST_REVIEWS {
        public String getComponent() {
            return "metric-list";
        }
        public String getName() {
            return "Top " + APIConfig.TOP_X_METRIC + " Newest Reviews";
        }
        public String getDescription() {
            return "Across all product categories";
        }
        public String getPagePosition() {
            return "middle";
        }
        public String getIcon() {
            return "refresh icon";
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
            return "Across all tracked products types";
        }
        public String getPagePosition() {
            return "bottom";
        }
        public String getIcon() {
            return "cubes icon";
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
            return "Across all tracked products types";
        }
        public String getPagePosition() {
            return "bottom";
        }
        public String getIcon() {
            return "heart icon";
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
            return "A warning to determine if more disk space is needed";
        }
        public String getPagePosition() {
            return "bottom";
        }
        public String getIcon() {
            return "dashboard icon";
        }
    };

    abstract String getComponent();
    abstract String getIcon();
    abstract String getName();
    abstract String getDescription();
    abstract String getPagePosition();
}
