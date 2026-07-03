local stock = tonumber(redis.call('GET', KEYS[1]))

--[[ no key found in the redis  ]]
if stock == nil then
    return -1
end


local quantity = tonumber(ARGV[1])

--[[ insufficient stock ]]
if stock < quantity then
    return -2
end

return redis.call('DECRBY', KEYS[1], quantity)