import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import { Typography, Divider } from '@material-ui/core';
import { Link } from 'react-router-dom';
import SearchBox from './SearchBox.js';
import SearchList from './SearchList.js';
import 'typeface-roboto';
import Pagination from "react-js-pagination";
import 'bootstrap/dist/css/bootstrap.min.css';

const styles = theme => ({
    searchbox: theme.mixins.gutters({
        marginTop: theme.spacing.unit,
        paddingLeft: 0,
        width: 600,
        display: 'inline-block'
    }),
    logoText: {
        display: 'inline-block',
        verticalAlign: 'middle',
        marginRight: 20,
        marginLeft: 20,
    }, 
    logoTextLink: {
        textDecoration: 'none',
        color: 'black'
    },
    header: {
        paddingTop: 15,
        paddingBottom: 25,
        backgroundColor: 'rgb(250,250,250)'
    }
});

class Search extends Component {
    constructor(props) {
        super(props);

        this.state = {searchText: this.getQueryString(), prevSearchText: this.getQueryString(), items: null, activePage: this.getPage(), totalItems: 0};
        this.getSearchResults();
    }

    componentDidUpdate(prevProps, prevState) {
        const queryText = this.getQueryString();
        const page = this.getPage();

        if (queryText && queryText !== prevState.prevSearchText || page !== prevState.activePage) {
            this.setState({searchText: queryText, prevSearchText: queryText, activePage: page});
            this.getSearchResults();
        }
    }

    onSearchChange = (event) => {
        this.setState({searchText: event.target.value});
    }
    
    onSearchSubmit = (event) => {

        if (event.key === undefined || event.key === 'Enter') {
            const path = encodeURI(`/search?query=${this.state.searchText}&start=0`);
            this.props.history.push(path);
            event.preventDefault();
        }
    }

    getQueryString = () => {
        const parameters = decodeURI(this.props.location.search);
        const query = parameters.split("=")[1].split("&")[0];

        return query;
    };

    getStart = () => {
        const parameters = decodeURI(this.props.location.search);
        const start = parameters.split("=")[2];
        return parseInt(start);
    }

    getPage = () => {
        return (parseInt(this.getStart()) / 10) + 1;
    }

    getSearchResults = () => {
        const searchText = this.getQueryString();
        const start = this.getStart();
        
        // do nothing if empty input
        if (/^ *$/.test(searchText)) {
            return;
        }

        let url = `http://localhost:4567/search?query=${searchText}&start=${start}`;
        fetch(url).then(result => result.json())
            .then((items) => {
                this.setState({items: items.entries, totalItems: items.totalResults});
            });
    }

    handlePageChange = (pageNumber) => {
        this.setState({activePage: pageNumber});
        const start = (pageNumber - 1) * 10;
        const path = encodeURI(`/search?query=${this.state.prevSearchText}&start=${start}`);
        this.props.history.push(path);

    }

    render() {
        const { classes } = this.props;

        return (
            <div className="Search">
                <div className={classes.header}>
                    <Typography variant="headline" noWrap={true} className={classes.logoText}>
                        <Link to="/" className={classes.logoTextLink} style={{ textDecoration: 'none' }}>ESE</Link>
                    </Typography>
                    <SearchBox className={classes.searchbox} value={this.state.searchText} onSearchChange={this.onSearchChange} onSearchSubmit={this.onSearchSubmit}/>
                </div>
                <Divider />
                <SearchList searchResults={this.state.items} className={classes.searchList} />

                <Pagination
                    activePage = {this.state.activePage}
                    itemsCountPerPage = {10}
                    totalItemsCount = {this.state.totalItems}
                    pageRangeDisplayed = {10}
                    onChange = {this.handlePageChange}
                    hideFirstLastPages = {true}
                />
                    
            </div>
        );
    }
}

Search.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(Search);