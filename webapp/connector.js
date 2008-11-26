jQuery(document).ready(function($){
    /*
     * Initialize resource list
     */
    function init(){
        $.get('rest/db/mdr/connector/listResourcesWithDetails.xquery', function(data, status)
        {
            if (status == 'success')
            {
                $('#resource').html('');

                $(data).find('query_service[category=CDE], query_service[category=CONCEPT]').each(function(){
                    var v = $(this).attr('name');
                    if (v == 'cgMDR'){
                        $('#resource').append('<option value="'+v+'" selected="true">' + v + '</option>');
                    } else {
                        $('#resource').append('<option value="'+v+'">' + v + '</option>');
                    }
                });
                $('#btnSearch').attr('disabled', false);
            } else {
                alert('Fail to initialize resources.');
            }
        }, 'xml');
        
        window.scrollTo(0, 1);
    }
    /*
     * Make query
     */
    function makeQuery(){
        var term = $('#searchTerm').val();
        var resource = $('#resource').val();
        if (term.length != 0)
        {
            $('#status').html('Searching...').css('color', '#999999');
            $.ajax({
                type: 'GET',
                url: 'rest/db/mdr/connector/query.xquery',
                data: 'resource='+resource+'&term='+term,
                dataType: 'xml',
                success: querySucess,
                error: queryError
            });
        } else 
        {
            alert('No search term');
        }
        //return false;
    }
    
    $('#searchForm').bind('submit', function(){$('#btnSearch').click();return false;});
    $('#btnSearch').click(makeQuery);
    
    
    /*
     * When query success
     */
    function querySucess(data, status){
        $('#searchResult').html('');
        $('#searchResult').css('display', 'none');
        
        if ($(data).find('data-element').length > 0)
        {
            $(data).find('data-element').each(function(){
                var id = $('id', this).text();
                var name = $('preferred', this).text();
                var desc = $('definition', this).text();
                $('<li class="resultItem"></li>')
                    .append('<div class="preferred">' + name + '</div>')
                    .append('<div class="id">' + id + '</div>')
                    .append('<div class="definition">' + desc + '</div>')
                    .append(valueDomain($('values', this)))
                    .appendTo('#searchResult');
            });
        }
        else
        {
            $(data).find('concept').each(function(){
                var id = $('id', this).text();
                var name = $('preferred', this).text();
                var desc = $('definition', this).text();
                $('<li class="resultItem"></li>')
                    .append('<div class="preferred">' + name + '</div>')
                    .append('<div class="id">' + id + '</div>')
                    .append('<div class="definition">' + desc + '</div>')
                    .append(properties($('properties', this)))
                    .appendTo('#searchResult');
            });         
        }
        $('#searchResult li.resultItem:odd').css('background-color', '#eeeeee');

        $('#status').html('Completed').css('color', '#0000ff');
        $('#searchResult').css('display', 'inline');
    }
    
    /*
     * When query fail
     */
    function queryError(request, status, exception) {
        $('#status').html('Fail: ' + status).css('color', '#ff0000');
    }
    
    function valueDomain(values){
        var v = $('<table id="values"></table>');
        if ($('enumerated', values).text().length > 0){
            $('<tr class="valueItem"></tr>')
                .append('<th>code</th>')
                .append('<th>meaning</th>')
                .appendTo(v);
            $('enumerated valid-value', values).each(function(){
                var code = $('code', this).text();
                if (code.length == 0){
                    code = '&#160;';
                }
                
                var meaning = $('meaning', this).text();
                if (meaning.length == 0){
                    meaning = '&#160;';
                }
                
                $('<tr class="valueItem"></tr>')
                .append('<td class="code">' + code + '</td>')
                .append('<td class="meaning">' + meaning + '</td>')
                .appendTo(v);
            });
        } else if ($('non-enumerated', values)) {
            $('<tr class="valueItem"></tr>')
            .append('<th>data type</th>')
            .append('<th>units</th>')
            .appendTo(v);
            var dt = $('data-type', values).text();
            var units = $('units', values).text();
            $('<tr class="valueItem"></tr>')
            .append('<td class="dataType">' + dt + '</td>')
            .append('<td class="units">' + units + '</td>')
            .appendTo(v);
        }
        return v;        
    }
    
    function properties(properties)
    {
        var v = $('<table id="values"></table>');
        $('<tr class="valueItem"></tr>')
            .append('<th>property</th>')
            .append('<th>value</th>')
            .appendTo(v);
        $('property', properties).each(function(){
                var name = $('name', this).text();
                if (name.length == 0){
                    name = '&#160;';
                }
                
                var value = $('value', this).text();
                if (value.length == 0){
                    value = '&#160;';
                }
                
                $('<tr class="valueItem"></tr>')
                    .append('<td class="code">' + name + '</td>')
                    .append('<td class="meaning">' + value + '</td>')
                    .appendTo(v);
        });
        return v;
    }
    
    init();
});