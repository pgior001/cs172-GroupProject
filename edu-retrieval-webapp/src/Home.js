import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import { Typography, Button } from '@material-ui/core';
import 'typeface-roboto';
import SearchBox from './SearchBox.js';

const styles = (theme) => ({
    container: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    textField: {
        marginLeft: theme.spacing.unit,
        marginRight: theme.spacing.unit,
        width: 'fill-available',
        marginTop: theme.spacing.unit,
        marginBottom: theme.spacing.unit / 2,
    },
    menu: {
        width: 200,
    },
    paper: theme.mixins.gutters({
        marginTop: theme.spacing.unit,
        paddingLeft: 0,
        width: 400,
        margin: '0 auto',
    }),
    button: {
        width: 200,
        margin: '0 auto',
        marginTop: 40
    },
    root: {
        height: '100%',
        width: '100%',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center'
    },
    titleText: {
        color: 'black',
        margin: '0 auto',
        marginBottom: 20
    }
});

class Home extends Component {
    constructor(props) {
        super(props);
        this.state = {searchText: ''};
    }  
    
    onSearchChange = (event) => {
        this.setState({searchText: event.target.value});
    }
 
    onSearchSubmit = (event) => {
        // If search button is clicked or enter is pressed in the input textbox.
        if ((event.key === undefined || event.key === 'Enter')) {
            // do nothing if empty input
            if (/^ *$/.test(this.state.searchText)) {
                return;
            }
            
            const path = encodeURI(`/search?query=${this.state.searchText}&start=0`);
            this.props.history.push(path);
            event.preventDefault();
        }
    }

    render() {
        const { classes } = this.props;

        return (
            <div className={classes.root}>
                <Typography variant="display3" noWrap={true} className={classes.titleText}>
                Edu Search Engine
                </Typography>
                <SearchBox className={classes.paper} value={this.state.searchText} onSearchChange={this.onSearchChange} onSearchSubmit={this.onSearchSubmit}/>
                <Button onClick={this.onSearchSubmit} variant="raised" className={classes.button}>
                    Search
                </Button>
            </div>
        );
    }
}

Home.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(Home);