package pt.floraon.occurrences.fields.parsers;

import pt.floraon.authentication.entities.User;
import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.driver.FloraOnException;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.occurrences.Messages;
import pt.floraon.occurrences.entities.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parses a comma-separated list of user names (person names, not usernames)
 * Created by miguel on 12-02-2017.
 */
public class UserListParser implements FieldParser {
    private Map<String, String> userMap;
    private IFloraOn driver;
    private Boolean createUsers;

    /**
     *
     * @param userMap
     * @param driver
     * @param createUsers Set to true to automatically create user accounts for non-existing names; false to throw an error
     *                  in that case, null to ignore the user and keep on parsing the record.
     */
    public UserListParser(Map<String, String> userMap, IFloraOn driver, Boolean createUsers) {
        this.userMap = userMap;
        this.driver = driver;
        this.createUsers = createUsers;
    }

    @Override
    public void parseValue(String inputValue, String inputFieldName, Object bean) throws IllegalArgumentException, FloraOnException {
        if(inputValue == null || inputValue.trim().equals("")) return;
        Inventory occurrence = (Inventory) bean;

        String[] spl = inputValue.split("\\+");
        if(spl.length == 1) spl = inputValue.split(",");

        String tmp;
        List<String> userIds = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for(String username : spl) {
            tmp = username.trim();
            if(userMap.containsKey(tmp))
                userIds.add(userMap.get(tmp));
            else {
                User user = driver.getAdministration().getUser(username.trim());
                if(user == null) {
                    if(createUsers == null)
                        continue;
                    else if(!createUsers) {
                        errors.add(Messages.getString("error.2", username.trim()));
                        continue;
                    }
                    user = new User();
                    user.setName(username.trim());
                    String id = driver.getAdministration().createUser(user).getID();
                    user.setID(id);
                }
                userMap.put(user.getName().toLowerCase(), user.getID());
                userIds.add(user.getID());
            }
        }

        String[] ids = userIds.toArray(new String[userIds.size()]);
        switch (inputFieldName.toLowerCase()) {
            case "observers":
                occurrence.setObservers(ids);
                break;

            case "collectors":
                occurrence.setCollectors(ids);
                break;

            case "dets":
                occurrence.setDets(ids);
                break;

            default:
                errors.add(Messages.getString("error.1", inputFieldName));
        }

        if(errors.size() > 0)
            throw new FloraOnException(StringUtils.implode("; ", errors.toArray(new String[errors.size()])));
    }
}
