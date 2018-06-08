import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import 'typeface-roboto';

const styles = theme => ({
    list: {
        listStyleType: 'none',
        fontFamily: 'roboto',
        marginTop: 30,
        paddingLeft: 100,
        paddingRight: 100
    },
    item: {
        marginBottom: 25,
        
    },
    titleText: {
        textDecoration: 'none',
        color: '#0000EE',
        fontSize: 18,
        '&:hover': {
            textDecoration: 'underline'
        }
    },
    linkText: {
        color: 'rgb(44,99,43)',
        fontSize: 16
    },
    noResults: {
        fontSize: 18,
        textAlign: 'center',
        marginTop: 20
    }
});

class SearchList extends Component {
    constructor(props) {
        super(props);
    }
    
    generateList() {
        const { classes } = this.props;

        const results = this.props.searchResults; 
        if (results == null) {
            return null;
        } else if (results.length < 1) {
           return <div className={classes.noResults}>No results found</div>
        } else {
            const listItems = [];
            for (let i = 0; i < results.length; i++) {
                const url = `http://www.${results[i].url}`;
                const item = 
                <li className={classes.item} key={i}>
                    <div><a href={url} className={classes.titleText}>{results[i].title}</a></div>
                    <div className={classes.linkText}>{url}</div>
                </li>
                listItems.push(item);
            }
            return <ul className={classes.list}>{listItems}</ul>
        }
    }

    render() {
        return (
            this.generateList()
        );
    }
}


SearchList.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(SearchList);